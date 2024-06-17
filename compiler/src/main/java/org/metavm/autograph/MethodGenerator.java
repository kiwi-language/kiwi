package org.metavm.autograph;

import com.intellij.psi.PsiMethod;
import org.metavm.entity.BuiltinKlasses;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.entity.natives.NativeFunctions;
import org.metavm.expression.Expression;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.expression.Expressions;
import org.metavm.expression.TypeNarrower;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class MethodGenerator {

    private final Method method;
    private final LinkedList<ScopeInfo> scopes = new LinkedList<>();
    private final VariableTable variableTable = new VariableTable();
    private final TypeResolver typeResolver;
    private final ExpressionResolver expressionResolver;
    private final Set<String> generatedNames = new HashSet<>();
    private final TypeNarrower typeNarrower = new TypeNarrower(this::getExpressionType);
    private final Map<BranchNode, LinkedList<ScopeInfo>> condScopes = new IdentityHashMap<>();
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

    BranchNode createBranchNode(boolean inclusive) {
        return setNodeExprTypes(new BranchNode(null, nextName("Branch"), null, inclusive, scope().getLastNode(), scope()));
    }

    MergeNode createMerge() {
        scope().getLastNode();
        if ((scope().getLastNode() instanceof BranchNode branchNode)) {
            var mergeNode = setNodeExprTypes(new MergeNode(
                    null,
                    nextName("Merge"),
                    null,
                    branchNode,
                    KlassBuilder.newBuilder("MergeOutput", null).temporary().build(),
                    scope()
            ));
            mergeNode.mergeExpressionTypes(MergeNode.getExpressionTypeMap(branchNode));
            return mergeNode;
        } else {
            throw new InternalException("MergeNode must directly follow a BranchNode");
        }
    }

    TryNode createTry() {
        return new TryNode(null, nextName("Try"), null, scope().getLastNode(), scope());
    }

    String nextVarName(String name) {
        String varName = "__" + name + "__";
        var count = varNames.compute(varName, (k, v) -> v == null ? 1 : v + 1);
        return varName + count;
    }

    TryEndNode createTryEnd() {
        var node = new TryEndNode(
                null, nextName("TryEnd"), null,
                KlassBuilder.newBuilder("TryEndOutput", null)
                        .temporary().build(),
                (TryNode) scope().getLastNode(),
                scope()
        );
        FieldBuilder.newBuilder("exception", "exception", node.getKlass(),
                        Types.getNullableThrowableType())
                .build();
        return node;
    }


    ValueNode createValue(String name, Expression expression) {
        return setNodeExprTypes(new ValueNode(
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

    NewArrayNode createNewArray(ArrayType type, @Nullable Expression initialValue) {
        return setNodeExprTypes(new NewArrayNode(
                null, nextName("NewArray"), null,
                type,
                NncUtils.get(initialValue, Values::expression),
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

    void enterCondSection(BranchNode sectionId) {
        variableTable.enterCondSection(sectionId);
        condScopes.put(sectionId, new LinkedList<>());
    }

    void enterBranch(Branch branch) {
        var exprTypeMap = variableTable.nextBranch(branch.getOwner(), branch);
        var scopeInfo = enterScope(branch.getScope(), exprTypeMap);
        condScopes.get(branch.getOwner()).add(scopeInfo);
    }

    void exitBranch() {
        exitScope();
    }

    void setYield(Expression yield) {
        variableTable.setYield(yield);
    }

    Map<Branch, Map<String, Expression>> exitCondSection(MergeNode mergeNode, List<String> outputVars) {
        return exitCondSection(mergeNode, outputVars, false);
    }

    Map<Branch, Map<String, Expression>> exitCondSection(MergeNode mergeNode, List<String> outputVars, boolean isSwitchExpression) {
        var branchNode = mergeNode.getBranchNode();
        ExpressionTypeMap exprTypes = null;
        for (ScopeInfo scope : condScopes.remove(branchNode)) {
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
            mergeNode.mergeExpressionTypes(exprTypes);
        }
        var result = variableTable.exitCondSection(branchNode, outputVars);
        if (result.values().iterator().next().yield() != null) {
            var yields = result.values().stream().map(BranchInfo::yield)
                    .filter(Objects::nonNull)
                    .toList();
            var yieldType = Types.getUnionType(
                    yields.stream().map(Expression::getType).collect(Collectors.toSet())
            );
            var yieldField = FieldBuilder.newBuilder("yield", "yield", mergeNode.getKlass(), yieldType).build();
            new MergeNodeField(
                    yieldField, mergeNode,
                    branchNode.getBranches()
                            .stream()
                            .filter(b -> !b.isTerminating())
                            .collect(Collectors.toMap(
                                    java.util.function.Function.identity(),
                                    b -> Values.expression(
                                            requireNonNull(result.get(b).yield(),
                                                    "Yield must be present in all non-terminating branches")
                                    )
                            ))
            );
            if (isInsideBranch() && !isSwitchExpression) {
                setYield(Expressions.nodeProperty(mergeNode, yieldField));
            }
        }
        return result.keySet().stream().collect(Collectors.toMap(
                java.util.function.Function.identity(),
                b -> result.get(b).variables()
        ));
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

    CheckNode createCheck(Expression condition, BranchNode exit) {
        var checkNode = setNodeExprTypes(
                new CheckNode(null, nextName("Check"), null, scope().getLastNode(), scope(),
                        Values.expression(condition), exit)
        );
        var narrower = new TypeNarrower(checkNode.getExpressionTypes()::getType);
        checkNode.mergeExpressionTypes(narrower.narrowType(condition));
        var exitExprTypes = checkNode.getExpressionTypes().merge(narrower.narrowType(Expressions.not(condition)));
        variableTable.addBranchEntry(exitExprTypes, exit);
        return checkNode;
    }

    private ScopeRT scope() {
        return currentScope().scope;
    }

    UpdateObjectNode createUpdateObject(Expression objectId) {
        return setNodeExprTypes(new UpdateObjectNode(null, nextName("Update"), null, scope().getLastNode(), scope(), Values.expression(objectId), List.of()));
    }

    UpdateObjectNode createUpdate(Expression self, Map<Field, Expression> fields) {
        var node = createUpdateObject(self);
        fields.forEach((field, value) -> node.setUpdateField(field, UpdateOp.SET, Values.expression(value)));
        return setNodeExprTypes(node);
    }

    UpdateStaticNode createUpdateStatic(Klass klass, Map<Field, Expression> fields) {
        var node = new UpdateStaticNode(
                null, nextName("UpdateStatic"), null,
                scope().getLastNode(), scope(), klass,
                List.of());
        fields.forEach((field, expr) -> node.setUpdateField(field, UpdateOp.SET, Values.expression(expr)));
        return setNodeExprTypes(node);
    }

    AddObjectNode createAddObject(Klass klass) {
        return setNodeExprTypes(new AddObjectNode(null, nextName("Add"),
                null, false, false, klass.getType(),
                scope().getLastNode(), scope()));
    }

    AddElementNode createAddElement(Expression array, Expression value) {
        return setNodeExprTypes(
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
        return setNodeExprTypes(
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
        return setNodeExprTypes(
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
        var node = new ReturnNode(null,
                nextName("Exit"),
                null,
                scope().getLastNode(),
                scope(),
                NncUtils.get(value, Values::expression)
        );
        return setNodeExprTypes(node);
    }

    ForeachNode createForEach(Expression array) {
        var loopType = newTemporaryType("ForeachOutput");
        newTemproryField(loopType, "array", array.getType());
        newTemproryField(loopType, "index", ModelDefRegistry.getType(Long.class));
        return setNodeExprTypes(new ForeachNode(
                null, nextName("Foreach"), null, loopType,
                scope().getLastNode(), scope(), Values.expression(array),
                Values.expression(Expressions.trueExpression())
        ));
    }

    IndexCountNode createIndexCount(Index index, IndexQueryKey from, IndexQueryKey to) {
        return setNodeExprTypes(new IndexCountNode(
                null, nextName("IndexCount"), null, scope().getLastNode(), scope(),
                index, from, to
        ));
    }

    IndexScanNode createIndexScan(Index index, IndexQueryKey from, IndexQueryKey to) {
        var arrayType = new ArrayType(index.getDeclaringType().getType(), ArrayKind.READ_ONLY);
        return setNodeExprTypes(new IndexScanNode(
                null, nextName("IndexScan"), null, arrayType, scope().getLastNode(),
                scope(), index, from, to
        ));
    }

    public IndexSelectNode createIndexSelect(Index index, IndexQueryKey key) {
        var listType = BuiltinKlasses.arrayList.get().getParameterized(List.of(index.getDeclaringType().getType()));
        return setNodeExprTypes(new IndexSelectNode(
                null, nextName("IndexSelect"), null, listType.getType(),
                scope().getLastNode(), scope(), index, key
        ));
    }

    public IndexSelectFirstNode createIndexSelectFirst(Index index, IndexQueryKey key) {
        return setNodeExprTypes(new IndexSelectFirstNode(
                null, nextName("IndexSelectFirst"), null,
                scope().getLastNode(), scope(), index, key
        ));
    }

    public Field newTemproryField(Klass klass, String name, Type type) {
        return FieldBuilder.newBuilder(name, null, klass, type).build();
    }

    public Klass newTemporaryType(String namePrefix) {
        String name = namePrefix + "_" + NncUtils.randomNonNegative();
        return KlassBuilder.newBuilder(name, null)
                .anonymous(true)
                .ephemeral(true)
                .build();
    }

    MethodCallNode createMethodCall(Expression self, Method method, List<Expression> arguments) {
        return createMethodCall(self, method, arguments, List.of(), List.of());
    }

    MethodCallNode createMethodCall(Expression self, Method method, List<Expression> arguments,
                                    List<Type> capturedExpressionTypes,
                                    List<Expression> capturedExpressions) {
        List<Argument> args = NncUtils.biMap(
                method.getParameters(), arguments,
                (param, arg) -> new Argument(null, param.getRef(), Values.expression(arg))
        );
        var node = new MethodCallNode(null, nextName(method.getName()), null,
                scope().getLastNode(), scope(),
                NncUtils.get(self, Values::expression), method.getRef(), args);
        node.setCapturedExpressions(capturedExpressions);
        node.setCapturedExpressionTypes(capturedExpressionTypes);
        return setNodeExprTypes(node);
    }

    NodeRT createTypeCast(Expression operand, Type targetType) {
        if (operand.getType().isNullable() && !targetType.isNullable())
            targetType = new UnionType(Set.of(targetType, Types.getNullType()));
        return createFunctionCall(
                NativeFunctions.typeCast.get().getParameterized(List.of(targetType)),
                List.of(operand)
        );
    }

    FunctionCallNode createFunctionCall(Function function, List<Expression> arguments) {
        var functionRef = function.getRef();
        List<Argument> args = NncUtils.biMap(
                functionRef.getRawFlow().getParameters(), arguments,
                (param, arg) -> new Argument(null, param.getRef(), Values.expression(arg))
        );
        return setNodeExprTypes(new FunctionCallNode(
                null,
                nextName(functionRef.getRawFlow().getName()),
                null,
                scope().getLastNode(), scope(),
                functionRef, args));
    }

    LambdaNode createLambda(List<Parameter> parameters, Type returnType, Klass functionalInterface) {
        var node = new LambdaNode(
                null, nextName("Lambda"), null, scope().getLastNode(), scope(),
                parameters, returnType, functionalInterface.getType()
        );
        node.createSAMImpl();
        return node;
    }

    NewObjectNode createNew(Method method, List<Expression> arguments, boolean ephemeral) {
        var methodRef = method.getRef();
        List<Argument> args = NncUtils.biMap(
                methodRef.getRawFlow().getParameters(), arguments,
                (param, arg) -> new Argument(null, param.getRef(), Values.expression(arg))
        );
        return setNodeExprTypes(new NewObjectNode(null, nextName(methodRef.resolve().getName()), null, methodRef, args,
                scope().getLastNode(), scope(), null, ephemeral, false));
    }

    ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public InputNode createInput() {
        var type = KlassBuilder.newBuilder("Input", null).temporary().build();
        return setNodeExprTypes(new InputNode(null, nextName("input"), null, type, scope().getLastNode(), scope()));
    }

    public SelfNode createSelf() {
        return setNodeExprTypes(new SelfNode(null, nextName("self"), null,
                ((Method) scope().getFlow()).getDeclaringType().getType(),
                scope().getLastNode(), scope()));
    }

    public <T extends NodeRT> T setNodeExprTypes(T node) {
        var scope = scope();
        var lastNode = scope.getLastNode();
        if (lastNode == null) {
            node.mergeExpressionTypes(scope.getExpressionTypes());
        } else {
            node.mergeExpressionTypes(lastNode.getExpressionTypes());
        }
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
        var node = setNodeExprTypes(new RaiseNode(
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

    public WhileNode createWhile() {
        return createWhile(Expressions.trueExpression());
    }

    public WhileNode createWhile(Expression condition) {
        return setNodeExprTypes(new WhileNode(
                null, nextName("While"), null,
                newTemporaryType("WhileOutput"),
                scope().getLastNode(),
                scope(),
                Values.expression(condition)
        ));
    }

    public PsiMethod getJavaMethod() {
        return null;
    }

    public ClearArrayNode createClearArray(Expression array) {
        return setNodeExprTypes(new ClearArrayNode(
                null, nextName("ClearArray"), null,
                scope().getLastNode(), scope(), Values.expression(array)
        ));
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
