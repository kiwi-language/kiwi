package org.metavm.autograph;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiVariable;
import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expression;
import org.metavm.expression.ExpressionTypeMap;
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

    Type getThisType() {
        return method.getDeclaringType().getType();
    }

    IfNode createIf(Value condition, @Nullable NodeRT target) {
        return onNodeCreated(new IfNode(null,
                nextName("if"),
                null,
                scope().getLastNode(),
                scope(),
                condition,
                target
        ));
    }

    IfNotNode createIfNot(Value condition, @Nullable NodeRT target) {
        return onNodeCreated(new IfNotNode(null,
                nextName("ifNot"),
                null,
                scope().getLastNode(),
                scope(),
                condition,
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

    TryEnterNode createTryEnter() {
        return onNodeCreated(new TryEnterNode(null, nextName("tryEnter"), null, scope().getLastNode(), scope()));
    }

    String nextVarName(String name) {
        String varName = "__" + name + "__";
        var count = varNames.compute(varName, (k, v) -> v == null ? 1 : v + 1);
        return varName + count;
    }

    TryExitNode createTryExit() {
        var node = onNodeCreated(new TryExitNode(
                null, nextName("tryExit"), null,
                KlassBuilder.newBuilder("TryExitOutput", null)
                        .temporary().build(),
                scope().getLastNode(),
                scope()
        ));
        FieldBuilder.newBuilder("exception", "exception", node.getKlass(),
                        Types.getNullableThrowableType())
                .build();
        return node;
    }


    ValueNode createValue(String name, Value expression) {
        return onNodeCreated(new ValueNode(
                        null,
                        nextName(name),
                        null,
                        getExpressionType(expression.getExpression()),
                        scope().getLastNode(),
                        scope(),
                        expression
                )
        );
    }

    NonNullNode createNonNull(String name, Value expression) {
        var node = onNodeCreated(new NonNullNode(
                        null,
                        nextName(name),
                        null,
                        Types.getNonNullType(getExpressionType(expression.getExpression())),
                        scope().getLastNode(),
                        scope(),
                        expression
                )
        );
        variableTable.processRaiseNode(node);
        return node;
    }

    TargetNode createTarget() {
        return onNodeCreated(new TargetNode(null, nextName("target"), null, scope().getLastNode(), scope()));
    }

    NewArrayNode createNewArray(ArrayType type, @Nullable Value initialValue) {
        return onNodeCreated(new NewArrayNode(
                null, nextName("NewArray"), null,
                type,
                initialValue,
                null,
                null,
                scope().getLastNode(), scope()
        ));
    }

    NewArrayNode createNewArrayWithDimensions(ArrayType type, List<Value> dimensions) {
        return onNodeCreated(new NewArrayNode(
                null, nextName("NewArray"), null,
                type,
                null,
                dimensions,
                null,
                scope().getLastNode(), scope()
        ));
    }

    ArrayLengthNode createArrayLength(Value array) {
        return new ArrayLengthNode(
                null, nextName("length"), null,
                scope().getLastNode(),
                scope(),
                array
        );
    }

    void enterTrySection(TryEnterNode tryEnterNode) {
        variableTable.enterTrySection(tryEnterNode);
    }

    Map<NodeRT, Map<String, Value>> exitTrySection(TryEnterNode tryEnterNode, List<String> outputVars) {
        return variableTable.exitTrySection(tryEnterNode, outputVars);
    }

    void enterCondSection(NodeRT sectionId) {
        variableTable.enterCondSection(sectionId);
        condScopes.put(sectionId, new LinkedList<>());
    }

    void enterBranch(NodeRT sectionId, long branchIndex) {
        variableTable.nextBranch(sectionId, branchIndex);
    }

    void setYield(Value yield) {
        variableTable.setYield(yield);
    }

    Map<Long, Map<String, Value>> exitCondSection(NodeRT sectionId,
                         JoinNode joinNode,
                         Map<Long, NodeRT> exits,
                         boolean isSwitchExpression) {
        ExpressionTypeMap exprTypes = null;
        for (ScopeInfo scope : condScopes.remove(sectionId)) {
            var lastNode = scope.scope.getLastNode();
            if (lastNode == null || !lastNode.isExit()) {
                var newExprTypes = lastNode == null ? ExpressionTypeMap.EMPTY : lastNode.getNextExpressionTypes();
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
                    yields.stream().map(Value::getType).collect(Collectors.toSet())
            );
            var yieldField = FieldBuilder.newBuilder("yield", "yield", joinNode.getKlass(), yieldType).build();
            var values = new HashMap<NodeRT, Value>();
            exits.forEach((branchIdx, exit) -> {
                if(exit.isSequential() || exit instanceof GotoNode g && g.getTarget() == joinNode) {
                    values.put(exit, result.get(branchIdx).yield());
                }
            });
            new JoinNodeField(yieldField, joinNode, values);
            if (isInsideBranch() && !isSwitchExpression) {
                setYield(Values.node(createNodeProperty(joinNode, yieldField)));
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
    }

    boolean isInsideBranch() {
        return variableTable.isInsideBranch();
    }

    private ScopeInfo currentScope() {
        return NncUtils.requireNonNull(scopes.peek());
    }

    void defineVariable(String name) {
        variableTable.define(name);
    }

    Value getThis() {
        return new NodeValue(createLoadThis());
    }

    ScopeInfo enterScope(ScopeRT scope) {
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
                return expression.getType();
            } else {
                return lastNode.getNextExpressionTypes().getType(expression);
            }
        }
    }

    ScopeRT scope() {
        return currentScope().scope;
    }

    UpdateObjectNode createUpdateObject(Value objectId) {
        return onNodeCreated(new UpdateObjectNode(null, nextName("Update"), null, scope().getLastNode(), scope(), objectId, List.of()));
    }

    UpdateObjectNode createUpdate(Value self, Map<Field, Value> fields) {
        var node = createUpdateObject(self);
        fields.forEach((field, value) -> node.setUpdateField(field, UpdateOp.SET, value));
        return node;
    }

    UpdateStaticNode createUpdateStatic(Klass klass, Map<Field, Value> fields) {
        var node = onNodeCreated(new UpdateStaticNode(
                null, nextName("UpdateStatic"), null,
                scope().getLastNode(), scope(), klass,
                List.of()));
        fields.forEach((field, expr) -> node.setUpdateField(field, UpdateOp.SET, expr));
        return node;
    }

    AddObjectNode createAddObject(Klass klass) {
        return onNodeCreated(new AddObjectNode(null, nextName("Add"),
                null, false, false, klass.getType(),
                scope().getLastNode(), scope()));
    }

    AddElementNode createAddElement(Value array, Value value) {
        return onNodeCreated(
                new AddElementNode(
                        null,
                        nextName("AddElement"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        array,
                        value
                )
        );
    }

    RemoveElementNode createRemoveElement(Value array, Value element) {
        return onNodeCreated(
                new RemoveElementNode(
                        null,
                        nextName("RemoveElement"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        array,
                        element
                )
        );
    }

    GetElementNode createGetElement(Value array, Value index) {
        return onNodeCreated(
                new GetElementNode(
                        null,
                        nextName("GetElement"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        array,
                        index
                )
        );
    }

    @SuppressWarnings("UnusedReturnValue")
    ReturnNode createReturn() {
        return createReturn(null);
    }

    ReturnNode createReturn(Value value) {
        return onNodeCreated(new ReturnNode(null,
                nextName("Exit"),
                null,
                scope().getLastNode(),
                scope(),
                value
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

    MethodCallNode createMethodCall(Value self, Method method, List<Value> arguments) {
        return createMethodCall(self, method, arguments, List.of(), List.of());
    }

    MethodCallNode createMethodCall(Value self, Method method, List<Value> arguments,
                                    List<Type> capturedExpressionTypes,
                                    List<Expression> capturedExpressions) {
        if(method.getParameters().size() != arguments.size()) {
            throw new IllegalArgumentException(
                    "Method " + method.getTypeDesc() + " expects " + method.getParameters().size() +
                            " arguments, but " + arguments.size() + " were provided");
        }
        List<Argument> args = NncUtils.biMap(
                method.getParameters(), arguments,
                (param, arg) -> new Argument(null, param.getRef(), arg)
        );
        var node = new MethodCallNode(null, nextName(method.getName()), null,
                scope().getLastNode(), scope(),
                self, method.getRef(), args);
        node.setCapturedExpressions(capturedExpressions);
        node.setCapturedExpressionTypes(capturedExpressionTypes);
        variableTable.processRaiseNode(node);
        return onNodeCreated(node);
    }

    NodeRT createTypeCast(Value operand, Type targetType) {
        if (getExpressionType(operand.getExpression()).isNullable() && !targetType.isNullable())
            targetType = new UnionType(Set.of(targetType, Types.getNullType()));
        return createFunctionCall(
                StdFunction.typeCast.get().getParameterized(List.of(targetType)),
                List.of(operand)
        );
    }

    FunctionCallNode createFunctionCall(Function function, List<Value> arguments) {
        var functionRef = function.getRef();
        List<Argument> args = NncUtils.biMap(
                functionRef.getRawFlow().getParameters(), arguments,
                (param, arg) -> new Argument(null, param.getRef(), arg)
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

    LambdaNode createLambda(Lambda lambda, Klass functionalInterface) {
        var node = onNodeCreated(new LambdaNode(
                null, nextName("lambda"), null, scope().getLastNode(), scope(),
                lambda, functionalInterface.getType()
        ));
        node.createSAMImpl();
        return node;
    }

    NewObjectNode createNew(Method method, List<Value> arguments, boolean ephemeral, boolean unbound) {
        var methodRef = method.getRef();
        List<Argument> args = NncUtils.biMap(
                methodRef.getRawFlow().getParameters(), arguments,
                (param, arg) -> new Argument(null, param.getRef(), arg)
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
    public RaiseNode createRaise(Value exception) {
        var node = onNodeCreated(new RaiseNode(
                null,
                nextName("Error"),
                null,
                scope().getLastNode(), scope(), RaiseParameterKind.THROWABLE,
                exception,
                null
        ));
        variableTable.processRaiseNode(node);
        return node;
    }

    public PsiMethod getJavaMethod() {
        return null;
    }

    public ClearArrayNode createClearArray(Value array) {
        return onNodeCreated(new ClearArrayNode(
                null, nextName("ClearArray"), null,
                scope().getLastNode(), scope(), array
        ));
    }

    public SetElementNode createSetElement(Value array, Value index, Value value) {
        return onNodeCreated(new SetElementNode(null, nextName("SetElement"), null,
                scope().getLastNode(), scope(), array,
                index, value));
    }

    AddNode createAdd(Value first, Value second) {
        return onNodeCreated(new AddNode(
                        null,
                        nextName("add"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    SubNode createSub(Value first, Value second) {
        return onNodeCreated(new SubNode(
                        null,
                        nextName("sub"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    MultiplyNode createMul(Value first, Value second) {
        return onNodeCreated(new MultiplyNode(
                        null,
                        nextName("mul"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    DivideNode createDiv(Value first, Value second) {
        return onNodeCreated(new DivideNode(
                        null,
                        nextName("div"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    LeftShiftNode createLeftShift(Value first, Value second) {
        return onNodeCreated(new LeftShiftNode(
                        null,
                        nextName("leftShift"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    RightShiftNode createRightShift(Value first, Value second) {
        return onNodeCreated(new RightShiftNode(
                        null,
                        nextName("rightShift"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    UnsignedRightShiftNode createUnsignedRightShift(Value first, Value second) {
        return onNodeCreated(new UnsignedRightShiftNode(
                        null,
                        nextName("unsignedRightShift"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    BitwiseOrNode createBitwiseOr(Value first, Value second) {
        return onNodeCreated(new BitwiseOrNode(
                        null,
                        nextName("bitwiseOr"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    BitwiseAndNode createBitwiseAnd(Value first, Value second) {
        return onNodeCreated(new BitwiseAndNode(
                        null,
                        nextName("bitwiseAnd"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    BitwiseXorNode createBitwiseXor(Value first, Value second) {
        return onNodeCreated(new BitwiseXorNode(
                        null,
                        nextName("bitwiseXor"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    AndNode createAnd(Value first, Value second) {
        return onNodeCreated(new AndNode(
                        null,
                        nextName("and"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    OrNode createOr(Value first, Value second) {
        return onNodeCreated(new OrNode(
                        null,
                        nextName("or"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    RemainderNode createRem(Value first, Value second) {
        return onNodeCreated(new RemainderNode(
                        null,
                        nextName("rem"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    EqNode createEq(Value first, Value second) {
        return onNodeCreated(new EqNode(
                        null,
                        nextName("eq"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    NeNode createNe(Value first, Value second) {
        return onNodeCreated(new NeNode(
                        null,
                        nextName("ne"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    GeNode createGe(Value first, Value second) {
        return onNodeCreated(new GeNode(
                        null,
                        nextName("ge"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    GtNode createGt(Value first, Value second) {
        return onNodeCreated(new GtNode(
                        null,
                        nextName("gt"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    LtNode createLt(Value first, Value second) {
        return onNodeCreated(new LtNode(
                        null,
                        nextName("lt"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    LeNode createLe(Value first, Value second) {
        return onNodeCreated(new LeNode(
                        null,
                        nextName("le"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        first,
                        second
                )
        );
    }

    InstanceOfNode createInstanceOf(Value operand, Type type) {
        return onNodeCreated(new InstanceOfNode(
                        null,
                        nextName("instanceOf"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        operand,
                        type
                )
        );
    }

    BitwiseComplementNode createBitwiseComplement(Value operand) {
        return onNodeCreated(new BitwiseComplementNode(
                        null,
                        nextName("bitwiseComplement"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        operand
                )
        );
    }

    NotNode createNot(Value operand) {
        return onNodeCreated(new NotNode(
                        null,
                        nextName("not"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        operand
                )
        );
    }

    NegateNode createNegate(Value operand) {
        return onNodeCreated(new NegateNode(
                        null,
                        nextName("negate"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        operand
                )
        );
    }

    GetPropertyNode createNodeProperty(NodeRT node, Property property) {
        return createGetProperty(Values.node(node), property);
    }

    GetPropertyNode createGetProperty(Value instance, Property property) {
        return onNodeCreated(new GetPropertyNode(
                        null,
                        nextName("property"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        instance,
                        property.getRef()
                )
        );
    }

    GetStaticNode createGetStatic(Property property) {
        return onNodeCreated(new GetStaticNode(
                        null,
                        nextName("static"),
                        null,
                        scope().getLastNode(),
                        scope(),
                        property.getRef()
                )
        );
    }

    NoopNode createNoop() {
        return new NoopNode(null, nextName("noop"), null, scope().getLastNode(), scope());
    }

    public NodeRT createLoadThis() {
        return createLoad(0, getThisType());
    }

    public NodeRT createLoad(int index, Type type) {
        return onNodeCreated(new LoadNode(
                null,
                nextName("load"),
                null,
                type,
                scope().getLastNode(),
                scope(),
                index
        ));
    }

    public NodeRT createStore(PsiVariable variable, Value value) {
        return createStore(TranspileUtils.getVariableIndex(variable), value);
    }

    public NodeRT createStore(int index, Value value) {
        return onNodeCreated(new StoreNode(
                null,
                nextName("store"),
                null,
                scope().getLastNode(),
                scope(),
                index,
                value
        ));
    }

    public NodeRT createLoadContextSlot(int contextIndex, int slotIndex, Type type) {
        return onNodeCreated(new LoadContextSlotNode(
                null,
                nextName("store"),
                null,
                type,
                scope().getLastNode(),
                scope(),
                contextIndex,
                slotIndex
        ));
    }

    public NodeRT createStoreContextSlot(int contextIndex, int slotIndex, Value value) {
        return onNodeCreated(new StoreContextSlotNode(
                null,
                nextName("store"),
                null,
                scope().getLastNode(),
                scope(),
                contextIndex,
                slotIndex,
                value
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
