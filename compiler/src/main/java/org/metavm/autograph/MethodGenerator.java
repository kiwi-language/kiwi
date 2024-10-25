package org.metavm.autograph;

import com.intellij.psi.PsiMethod;
import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expression;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.expression.Expressions;
import org.metavm.expression.TypeNarrower;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MethodGenerator {

    private final Method method;
    private final LinkedList<ScopeInfo> scopes = new LinkedList<>();
    private final VariableTable variableTable = new VariableTable();
    private final TypeResolver typeResolver;
    private final ExpressionResolver expressionResolver;
    private final Set<String> generatedNames = new HashSet<>();
    private final TypeNarrower typeNarrower = new TypeNarrower(this::getExpressionType);
    private final Map<NodeRT, LinkedList<ScopeInfo>> condScopes = new IdentityHashMap<>();
    private final Map<String, Integer> varNames = new HashMap<>();

    public MethodGenerator(Method method, TypeResolver typeResolver, VisitorBase visitor) {
        this.method = method;
        this.typeResolver = typeResolver;
        expressionResolver = new ExpressionResolver(this, variableTable, typeResolver,
                visitor);
    }

    Method getMethod() {
        return method;
    }

    IfNode createIf(Expression condition, @Nullable NodeRT target) {
        return onNodeCreated(new IfNode(null,
                nextName("if"),
                null,
                scope().getLastNode(),
                scope(),
                Values.expression(condition),
                target
        ));
    }

    JoinNode createJoin() {
        return onNodeCreated(new JoinNode(
                null,
                nextName("join"),
                null,
                KlassBuilder.newBuilder("JoinOutput", null).temporary().build(),
                scope().getLastNode(),
                scope()
        ));
    }

    GotoNode createGoto(NodeRT target) {
        return onNodeCreated(new GotoNode(
                null,
                 nextName("goto"),
                null,
                scope().getLastNode(),
                scope(),
                target
        ));
    }

    GotoNode createIncompleteGoto() {
        return onNodeCreated(new GotoNode(
                null,
                nextName("goto"),
                null,
                scope().getLastNode(),
                scope()
        ));
    }

    TryNode createTry() {
        return onNodeCreated(new TryNode(null, nextName("try"), null, scope().getLastNode(), scope()));
    }

    String nextVarName(String name) {
        String varName = "__" + name + "__";
        var count = varNames.compute(varName, (k, v) -> v == null ? 1 : v + 1);
        return varName + count;
    }

    TryEndNode createTryEnd() {
        var node = onNodeCreated(new TryEndNode(
                null, nextName("TryEnd"), null,
                KlassBuilder.newBuilder("TryEndOutput", null)
                        .temporary().build(),
                (TryNode) scope().getLastNode(),
                scope()
        ));
        FieldBuilder.newBuilder("exception", "exception", node.getKlass(),
                        Types.getNullableThrowableType())
                .build();
        return node;
    }


    ValueNode createValue(String name, Expression expression) {
        return onNodeCreated(new ValueNode(
                        null,
                        nextName(name),
                        null,
                        getExpressionType(expression),
                        scope().getLastNode(),
                        scope(),
                        Values.expression(expression)
                )
        );
    }

    NonNullNode createNonNull(String name, Expression expression) {
        var node = onNodeCreated(new NonNullNode(
                        null,
                        nextName(name),
                        null,
                        Types.getNonNullType(getExpressionType(expression)),
                        scope().getLastNode(),
                        scope(),
                        Values.expression(expression)
                )
        );
        variableTable.processRaiseNode(node);
        return node;
    }

    TargetNode createTarget() {
        return onNodeCreated(new TargetNode(null, nextName("target"), null, scope().getLastNode(), scope()));
    }

    NewArrayNode createNewArray(ArrayType type, @Nullable Expression initialValue) {
        return onNodeCreated(new NewArrayNode(
                null, nextName("NewArray"), null,
                type,
                NncUtils.get(initialValue, Values::expression),
                null,
                null,
                scope().getLastNode(), scope()
        ));
    }

    NewArrayNode createNewArrayWithDimensions(ArrayType type, List<Expression> dimensions) {
        return onNodeCreated(new NewArrayNode(
                null, nextName("NewArray"), null,
                type,
                null,
                NncUtils.map(dimensions, Values::expression),
                null,
                scope().getLastNode(), scope()
        ));
    }

    void enterTrySection(TryNode tryNode) {
        variableTable.enterTrySection(tryNode);
        enterScope(tryNode.getBodyScope());
    }

    Map<NodeRT, Map<String, Expression>> exitTrySection(TryNode tryNode, List<String> outputVars) {
        exitScope();
        return variableTable.exitTrySection(tryNode, outputVars);
    }

    void enterCondSection(NodeRT sectionId) {
        variableTable.enterCondSection(sectionId);
        condScopes.put(sectionId, new LinkedList<>());
    }

    ExpressionTypeMap enterBranch(NodeRT sectionId, long branchIndex, Value condition) {
        return variableTable.nextBranch(sectionId, branchIndex, condition);
    }

    void setYield(Expression yield) {
        variableTable.setYield(yield);
    }

    Map<Long, Map<String, Expression>> exitCondSection(NodeRT sectionId,
                         JoinNode joinNode,
                         Map<Long, NodeRT> exits,
                         boolean isSwitchExpression) {
        ExpressionTypeMap exprTypes = null;
        for (ScopeInfo scope : condScopes.remove(sectionId)) {
            var lastNode = scope.scope.getLastNode();
            if (lastNode == null || !lastNode.isExit()) {
                var newExprTypes = lastNode == null ? scope.scope.getExpressionTypes() : lastNode.getExpressionTypes();
                if (exprTypes == null) {
                    exprTypes = newExprTypes;
                } else {
                    exprTypes = exprTypes.union(newExprTypes);
                }
            }
        }
        if (exprTypes != null) {
            joinNode.mergeExpressionTypes(exprTypes);
        }
        var result = variableTable.exitCondSection(sectionId);
        var hasYield = result.values().stream().anyMatch(b -> b.yield() != null);
        if (hasYield) {
            var yields = result.values().stream().map(BranchInfo::yield)
                    .filter(Objects::nonNull)
                    .toList();
            var yieldType = Types.getUnionType(
                    yields.stream().map(Expression::getType).collect(Collectors.toSet())
            );
            var yieldField = FieldBuilder.newBuilder("yield", "yield", joinNode.getKlass(), yieldType).build();
            var values = new HashMap<NodeRT, Value>();
            exits.forEach((branchIdx, exit) -> {
                if(exit.isSequential() || exit instanceof GotoNode g && g.getTarget() == joinNode) {
                    values.put(exit, Values.expression(result.get(branchIdx).yield()));
                }
            });
            new JoinNodeField(yieldField, joinNode, values);
            if (isInsideBranch() && !isSwitchExpression) {
                setYield(Expressions.nodeProperty(joinNode, yieldField));
            }
        }
        return result.keySet().stream().collect(Collectors.toMap(
                java.util.function.Function.identity(),
                b -> result.get(b).variables()
        ));
    }

    void exitCondSection1(NodeRT sectionId, JoinNode joinNode, Map<Long, NodeRT> exits, boolean isSwitchExpression) {
        var condOutputs = exitCondSection(sectionId, joinNode, exits, isSwitchExpression);
        var vars = condOutputs.values().iterator().next().keySet();
        out: for (var var : vars) {
            var branch2value = new HashMap<NodeRT, org.metavm.flow.Value>();
            for (var entry2 : condOutputs.entrySet()) {
                var branchIdx = entry2.getKey();
                var exit = Objects.requireNonNull(exits.get(branchIdx));
                if(exit.isExit() || (exit instanceof GotoNode g && g.getTarget() != joinNode))
                    continue;
                var branchOutputs = entry2.getValue();
                var v = branchOutputs.get(var);
//                var v = Objects.requireNonNull(,
//                        () -> "Variable " + var + " is missing from branch " + branch.getIndex());
                if(v == null)
                    continue out;
                branch2value.put(Objects.requireNonNull(exits.get(branchIdx)), Values.expression(v));
            }
            var memberTypes = new HashSet<Type>();
            for (var value : branch2value.values()) {
                if (NncUtils.noneMatch(memberTypes, t -> t.isAssignableFrom(value.getType())))
                    memberTypes.add(value.getType());
            }
            var fieldType =
                    memberTypes.size() == 1 ? memberTypes.iterator().next() : Types.getUnionType(memberTypes);
            var field = FieldBuilder.newBuilder(var, var, joinNode.getKlass(), fieldType).build();
            var joinField = new JoinNodeField(field, joinNode, branch2value);
            setVariable(var, Expressions.nodeProperty(joinNode, joinField.getField()));
        }
    }

    boolean isInsideBranch() {
        return variableTable.isInsideBranch();
    }

    private ScopeInfo currentScope() {
        return NncUtils.requireNonNull(scopes.peek());
    }

    void setVariable(String name, Expression value) {
        variableTable.set(name, value);
    }

    void defineVariable(String name) {
        variableTable.define(name);
    }

    Expression getVariable(String name) {
        return variableTable.get(name);
    }

    ScopeInfo enterScope(ScopeRT scope) {
        return enterScope(scope, NncUtils.get(scope.getOwner(), NodeRT::getExpressionTypes));
    }

    ScopeInfo enterScope(ScopeRT scope, ExpressionTypeMap expressionTypeMap) {
        if (expressionTypeMap != null) {
            scope.setExpressionTypes(expressionTypeMap);
        }
        var scopeInfo = new ScopeInfo(scope);
        scopes.push(scopeInfo);
        return scopeInfo;
    }

    ScopeInfo exitScope() {
        return scopes.pop();
    }

    Type getExpressionType(Expression expression) {
        if (scopes.isEmpty()) {
            return expression.getType();
        } else {
            var lastNode = scope().getLastNode();
            if (lastNode == null) {
                return scope().getExpressionTypes().getType(expression);
            } else {
                return lastNode.getExpressionTypes().getType(expression);
            }
        }
    }

    ScopeRT scope() {
        return currentScope().scope;
    }

    UpdateObjectNode createUpdateObject(Expression objectId) {
        return onNodeCreated(new UpdateObjectNode(null, nextName("Update"), null, scope().getLastNode(), scope(), Values.expression(objectId), List.of()));
    }

    UpdateObjectNode createUpdate(Expression self, Map<Field, Expression> fields) {
        var node = createUpdateObject(self);
        fields.forEach((field, value) -> node.setUpdateField(field, UpdateOp.SET, Values.expression(value)));
        return node;
    }

    UpdateStaticNode createUpdateStatic(Klass klass, Map<Field, Expression> fields) {
        var node = onNodeCreated(new UpdateStaticNode(
                null, nextName("UpdateStatic"), null,
                scope().getLastNode(), scope(), klass,
                List.of()));
        fields.forEach((field, expr) -> node.setUpdateField(field, UpdateOp.SET, Values.expression(expr)));
        return node;
    }

    AddObjectNode createAddObject(Klass klass) {
        return onNodeCreated(new AddObjectNode(null, nextName("Add"),
                null, false, false, klass.getType(),
                scope().getLastNode(), scope()));
    }

    AddElementNode createAddElement(Expression array, Expression value) {
        return onNodeCreated(
                new AddElementNode(
                        null,
                        nextName("AddElement"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        Values.expression(array),
                        Values.expression(value)
                )
        );
    }

    RemoveElementNode createRemoveElement(Expression array, Expression element) {
        return onNodeCreated(
                new RemoveElementNode(
                        null,
                        nextName("RemoveElement"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        Values.expression(array),
                        Values.expression(element)
                )
        );
    }

    GetElementNode createGetElement(Expression array, Expression index) {
        return onNodeCreated(
                new GetElementNode(
                        null,
                        nextName("GetElement"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        Values.expression(array),
                        Values.expression(index)
                )
        );
    }

    @SuppressWarnings("UnusedReturnValue")
    ReturnNode createReturn() {
        return createReturn(null);
    }

    ReturnNode createReturn(Expression value) {
        return onNodeCreated(new ReturnNode(null,
                nextName("Exit"),
                null,
                scope().getLastNode(),
                scope(),
                NncUtils.get(value, Values::expression)
        ));
    }

    IndexCountNode createIndexCount(Index index, IndexQueryKey from, IndexQueryKey to) {
        return onNodeCreated(new IndexCountNode(
                null, nextName("IndexCount"), null, scope().getLastNode(), scope(),
                index, from, to
        ));
    }

    IndexScanNode createIndexScan(Index index, IndexQueryKey from, IndexQueryKey to) {
        var arrayType = new ArrayType(index.getDeclaringType().getType(), ArrayKind.READ_ONLY);
        return onNodeCreated(new IndexScanNode(
                null, nextName("IndexScan"), null, arrayType, scope().getLastNode(),
                scope(), index, from, to
        ));
    }

    public IndexSelectNode createIndexSelect(Index index, IndexQueryKey key) {
        var listType = StdKlass.arrayList.get().getParameterized(List.of(index.getDeclaringType().getType()));
        return onNodeCreated(new IndexSelectNode(
                null, nextName("IndexSelect"), null, listType.getType(),
                scope().getLastNode(), scope(), index, key
        ));
    }

    public IndexSelectFirstNode createIndexSelectFirst(Index index, IndexQueryKey key) {
        return onNodeCreated(new IndexSelectFirstNode(
                null, nextName("IndexSelectFirst"), null,
                scope().getLastNode(), scope(), index, key
        ));
    }

    public Field newTemporaryField(Klass klass, String name, Type type) {
        return FieldBuilder.newBuilder(name, null, klass, type).build();
    }

    MethodCallNode createMethodCall(Expression self, Method method, List<Expression> arguments) {
        return createMethodCall(self, method, arguments, List.of(), List.of());
    }

    MethodCallNode createMethodCall(Expression self, Method method, List<Expression> arguments,
                                    List<Type> capturedExpressionTypes,
                                    List<Expression> capturedExpressions) {
        if(method.getParameters().size() != arguments.size()) {
            throw new IllegalArgumentException(
                    "Method " + method.getTypeDesc() + " expects " + method.getParameters().size() +
                            " arguments, but " + arguments.size() + " were provided");
        }
        List<Argument> args = NncUtils.biMap(
                method.getParameters(), arguments,
                (param, arg) -> new Argument(null, param.getRef(), Values.expression(arg))
        );
        var node = new MethodCallNode(null, nextName(method.getName()), null,
                scope().getLastNode(), scope(),
                NncUtils.get(self, Values::expression), method.getRef(), args);
        node.setCapturedExpressions(capturedExpressions);
        node.setCapturedExpressionTypes(capturedExpressionTypes);
        variableTable.processRaiseNode(node);
        return onNodeCreated(node);
    }

    NodeRT createTypeCast(Expression operand, Type targetType) {
        if (getExpressionType(operand).isNullable() && !targetType.isNullable())
            targetType = new UnionType(Set.of(targetType, Types.getNullType()));
        return createFunctionCall(
                StdFunction.typeCast.get().getParameterized(List.of(targetType)),
                List.of(operand)
        );
    }

    FunctionCallNode createFunctionCall(Function function, List<Expression> arguments) {
        var functionRef = function.getRef();
        List<Argument> args = NncUtils.biMap(
                functionRef.getRawFlow().getParameters(), arguments,
                (param, arg) -> new Argument(null, param.getRef(), Values.expression(arg))
        );
        var node = onNodeCreated(new FunctionCallNode(
                null,
                nextName(functionRef.getRawFlow().getName()),
                null,
                scope().getLastNode(), scope(),
                functionRef, args));
        variableTable.processRaiseNode(node);
        return node;
    }

    LambdaNode createLambda(List<Parameter> parameters, Type returnType, Klass functionalInterface) {
        var node = onNodeCreated(new LambdaNode(
                null, nextName("Lambda"), null, scope().getLastNode(), scope(),
                parameters, returnType, functionalInterface.getType()
        ));
        node.createSAMImpl();
        return node;
    }

    NewObjectNode createNew(Method method, List<Expression> arguments, boolean ephemeral, boolean unbound) {
        var methodRef = method.getRef();
        List<Argument> args = NncUtils.biMap(
                methodRef.getRawFlow().getParameters(), arguments,
                (param, arg) -> new Argument(null, param.getRef(), Values.expression(arg))
        );
        var node = onNodeCreated(new NewObjectNode(null, nextName(methodRef.resolve().getName()), null, methodRef, args,
                scope().getLastNode(), scope(), null, ephemeral, unbound));
        variableTable.processRaiseNode(node);
        return node;
    }

    ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public InputNode createInput() {
        var type = KlassBuilder.newBuilder("Input", null).temporary().build();
        return onNodeCreated(new InputNode(null, nextName("input"), null, type, scope().getLastNode(), scope()));
    }

    public SelfNode createSelf() {
        return onNodeCreated(new SelfNode(null, nextName("self"), null,
                ((Method) scope().getFlow()).getDeclaringType().getType(),
                scope().getLastNode(), scope()));
    }

    public <T extends NodeRT> T onNodeCreated(T node) {
        var scope = scope();
        var lastNode = scope.getLastNode();
        if (lastNode != null && lastNode.isSequential())
            node.mergeExpressionTypes(lastNode.getNextExpressionTypes());
        return node;
    }

    private String nextName(String nameRoot) {
        var pieces = nameRoot.split("_");
        int n;
        if (NncUtils.isDigits(pieces[pieces.length - 1])) {
            nameRoot = Arrays.stream(pieces).limit(pieces.length - 1).collect(Collectors.joining("_"));
            n = Integer.parseInt(pieces[pieces.length - 1]);
        } else {
            n = 0;
        }
        var newName = nameRoot;
        while (generatedNames.contains(newName)) {
            newName = nameRoot + "_" + ++n;
        }
        generatedNames.add(newName);
        return newName;
    }

    @SuppressWarnings("UnusedReturnValue")
    public RaiseNode createRaise(Expression exception) {
        var node = onNodeCreated(new RaiseNode(
                null,
                nextName("Error"),
                null,
                scope().getLastNode(), scope(), RaiseParameterKind.THROWABLE,
                Values.expression(exception),
                null
        ));
        variableTable.processRaiseNode(node);
        return node;
    }

    public PsiMethod getJavaMethod() {
        return null;
    }

    public ClearArrayNode createClearArray(Expression array) {
        return onNodeCreated(new ClearArrayNode(
                null, nextName("ClearArray"), null,
                scope().getLastNode(), scope(), Values.expression(array)
        ));
    }

    public SetElementNode createSetElement(Expression array, Expression index, Expression value) {
        return onNodeCreated(new SetElementNode(null, nextName("SetElement"), null,
                scope().getLastNode(), scope(), Values.expression(array),
                Values.expression(index), Values.expression(value)));
    }

    NoopNode createNoop() {
        return new NoopNode(null, nextName("noop"), null, scope().getLastNode(), scope());
    }

    private static final class ScopeInfo {
        private final ScopeRT scope;
//        private Map<Expression, Type> expressionTypes;

        private ScopeInfo(ScopeRT scope/*, Map<Expression, Type> expressionTypes*/) {
            this.scope = scope;
//            this.expressionTypes = expressionTypes;
        }

    }

}
