package tech.metavm.autograph;

import com.intellij.psi.PsiMethod;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.StandardTypes;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.*;
import tech.metavm.flow.*;
import tech.metavm.object.type.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

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
    private final FunctionTypeProvider functionTypeProvider;
    private final ParameterizedTypeProvider parameterizedTypeProvider;
    private final ArrayTypeProvider arrayTypeProvider;

    public MethodGenerator(Method method, TypeResolver typeResolver,
                           IEntityContext context,
                           VisitorBase visitor) {
        this(method, typeResolver, new ContextArrayTypeProvider(context), context.getUnionTypeContext(),
                context.getFunctionTypeContext(), context.getGenericContext(), context.getGenericContext(), visitor);
    }

    public MethodGenerator(Method method, TypeResolver typeResolver,
                           ArrayTypeProvider arrayTypeProvider,
                           UnionTypeProvider unionTypeProvider, FunctionTypeProvider functionTypeProvider,
                           ParameterizedTypeProvider parameterizedTypeProvider,
                           ParameterizedFlowProvider parameterizedFlowProvider,
                           VisitorBase visitor) {
        this.method = method;
        this.typeResolver = typeResolver;
        this.functionTypeProvider = functionTypeProvider;
        this.parameterizedTypeProvider = parameterizedTypeProvider;
        this.arrayTypeProvider = arrayTypeProvider;
        expressionResolver = new ExpressionResolver(this, variableTable, typeResolver,
                arrayTypeProvider,
                unionTypeProvider,
                parameterizedTypeProvider,
                parameterizedFlowProvider,
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
                    ClassTypeBuilder.newBuilder("合并节点输出", null).temporary().build(),
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
                ClassTypeBuilder.newBuilder("TryEndOutput", null)
                        .temporary().build(),
                (TryNode) scope().getLastNode(),
                scope()
        );
        FieldBuilder.newBuilder("异常", "exception", node.getType(),
                        StandardTypes.getNullableThrowableType())
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
        var result =  variableTable.exitCondSection(branchNode, outputVars);
        if(result.values().iterator().next().yield() != null) {
            var yields = result.values().stream().map(BranchInfo::yield)
                    .filter(Objects::nonNull)
                    .toList();
            var yieldType = Types.getUnionType(
                    yields.stream().map(Expression::getType).collect(Collectors.toSet()),
                    expressionResolver.getUnionTypeProvider()
            );
            var yieldField = FieldBuilder.newBuilder("yield", "yield", mergeNode.getType(), yieldType).build();
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
            if(isInsideBranch() && !isSwitchExpression) {
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
        return setNodeExprTypes(new UpdateObjectNode(null, nextName("Update"), null, scope().getLastNode(), scope(), Values.expression(objectId)));
    }

    UpdateObjectNode createUpdate(Expression self, Map<Field, Expression> fields) {
        var node = createUpdateObject(self);
        fields.forEach((field, value) -> node.setUpdateField(field, UpdateOp.SET, Values.expression(value)));
        return setNodeExprTypes(node);
    }

    UpdateStaticNode createUpdateStatic(ClassType klass, Map<Field, Expression> fields) {
        var node = new UpdateStaticNode(
                null, nextName("UpdateStatic"), null,
                scope().getLastNode(), scope(), klass
        );
        fields.forEach((field, expr) -> node.setUpdateField(field, UpdateOp.SET, Values.expression(expr)));
        return setNodeExprTypes(node);
    }

    AddObjectNode createAddObject(ClassType klass) {
        return setNodeExprTypes(new AddObjectNode(null, nextName("Add"),
                null, false, false, klass,
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
        var arrayType = arrayTypeProvider.getArrayType(index.getDeclaringType(), ArrayKind.READ_ONLY);
        return setNodeExprTypes(new IndexScanNode(
                null, nextName("IndexScan"), null, arrayType, scope().getLastNode(),
                scope(), index, from, to
        ));
    }

    public IndexSelectNode createIndexSelect(Index index, IndexQueryKey key) {
        var listType = parameterizedTypeProvider.getParameterizedType(
                StandardTypes.getReadWriteListType(), List.of(index.getDeclaringType()));
        return setNodeExprTypes(new IndexSelectNode(
                null, nextName("IndexSelect"), null, listType,
                scope().getLastNode(), scope(), index, key
        ));
    }

    public IndexSelectFirstNode createIndexSelectFirst(Index index, IndexQueryKey key) {
        return setNodeExprTypes(new IndexSelectFirstNode(
                null, nextName("IndexSelectFirst"), null,
                scope().getLastNode(), scope(), index, key
        ));
    }

    public Field newTemproryField(ClassType klass, String name, Type type) {
        return FieldBuilder.newBuilder(name, null, klass, type).build();
    }

    public ClassType newTemporaryType(String namePrefix) {
        String name = namePrefix + "_" + NncUtils.randomNonNegative();
        return ClassTypeBuilder.newBuilder(name, null)
                .anonymous(true)
                .ephemeral(true)
                .build();
    }

    MethodCallNode createMethodCall(Expression self, Method method, List<Expression> arguments) {
        List<Argument> args = NncUtils.biMap(
                method.getParameters(), arguments,
                (param, arg) -> new Argument(null, param, Values.expression(arg))
        );
        return setNodeExprTypes(new MethodCallNode(null, nextName(method.getName()), null,
                scope().getLastNode(), scope(),
                NncUtils.get(self, Values::expression), method, args));
    }

    NodeRT createTypeCast(Expression operand, Type targetType) {
        if(operand.getType().isNullable() && !targetType.isNullable())
            targetType = expressionResolver.getUnionTypeProvider().getUnionType(Set.of(targetType, StandardTypes.getNullType()));
        return createFunctionCall(
                expressionResolver.getParameterizedFlowProvider().getParameterizedFlow(NativeFunctions.getTypeCast(), List.of(targetType)),
                List.of(operand)
        );
    }

    FunctionCallNode createFunctionCall(Function function, List<Expression> arguments) {
        List<Argument> args = NncUtils.biMap(
                function.getParameters(), arguments,
                (param, arg) -> new Argument(null, param, Values.expression(arg))
        );
        return setNodeExprTypes(new FunctionCallNode(
                null,
                nextName(function.getEffectiveHorizontalTemplate().getName()),
                null,
                scope().getLastNode(), scope(),
                function, args));
    }

    LambdaNode createLambda(List<Parameter> parameters, Type returnType, ClassType functionalInterface) {
        var funcType = functionTypeProvider.getFunctionType(
                NncUtils.map(parameters, Parameter::getType), returnType
        );
        var node = new LambdaNode(
                null, nextName("Lambda"), null, scope().getLastNode(), scope(),
                parameters, returnType, funcType, functionalInterface
        );
        node.createSAMImpl(functionTypeProvider, parameterizedTypeProvider);
        return node;
    }

    NewObjectNode createNew(Flow flow, List<Expression> arguments, boolean ephemeral) {
        List<Argument> args = NncUtils.biMap(
                flow.getParameters(), arguments,
                (param, arg) -> new Argument(null, param, Values.expression(arg))
        );
        return setNodeExprTypes(new NewObjectNode(null, nextName(flow.getName()), null, flow, args,
                scope().getLastNode(), scope(), null, ephemeral));
    }

    ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public InputNode createInput() {
        var type = ClassTypeBuilder.newBuilder("输入类型", null).temporary().build();
        return setNodeExprTypes(new InputNode(null, nextName("Input"), null, type, scope().getLastNode(), scope()));
    }

    public SelfNode createSelf() {
        return setNodeExprTypes(new SelfNode(null, nextName("Self"), null,
                SelfNode.getSelfType((Method) scope().getFlow(), parameterizedTypeProvider),
                scope().getLastNode(), scope()));
    }

    public  <T extends NodeRT> T setNodeExprTypes(T node) {
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
        if(NncUtils.isDigits(pieces[pieces.length-1])) {
            nameRoot = Arrays.stream(pieces).limit(pieces.length-1).collect(Collectors.joining("_"));
            n = Integer.parseInt(pieces[pieces.length-1]);
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
