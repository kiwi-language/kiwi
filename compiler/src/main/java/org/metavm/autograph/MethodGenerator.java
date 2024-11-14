package org.metavm.autograph;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiVariable;
import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expression;
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
    private final TypeResolver typeResolver;
    private final ExpressionResolver expressionResolver;
    private final Set<String> generatedNames = new HashSet<>();
    private final TypeNarrower typeNarrower = new TypeNarrower(this::getExpressionType);
    private final Map<String, Integer> varNames = new HashMap<>();
    private final LinkedList<Integer> yieldVariables = new LinkedList<>();

    public MethodGenerator(Method method, TypeResolver typeResolver, VisitorBase visitor) {
        this.method = method;
        this.typeResolver = typeResolver;
        expressionResolver = new ExpressionResolver(this, typeResolver,
                visitor);
    }

    Method getMethod() {
        return method;
    }

    ClassType getThisType() {
        return method.getDeclaringType().getType();
    }

    IfNode createIf( @Nullable NodeRT target) {
        return onNodeCreated(new IfNode(null,
                nextName("if"),
                scope().getLastNode(),
                scope(),
                target
        ));
    }

    IfNotNode createIfNot(@Nullable NodeRT target) {
        return onNodeCreated(new IfNotNode(null,
                nextName("ifNot"),
                scope().getLastNode(),
                scope(),
                target
        ));
    }

    GotoNode createGoto(NodeRT target) {
        return onNodeCreated(new GotoNode(
                null,
                 nextName("goto"),
                scope().getLastNode(),
                scope(),
                target
        ));
    }

    GotoNode createIncompleteGoto() {
        return onNodeCreated(new GotoNode(
                null,
                nextName("goto"),
                scope().getLastNode(),
                scope()
        ));
    }

    TryEnterNode createTryEnter() {
        return onNodeCreated(new TryEnterNode(null, nextName("tryEnter"), scope().getLastNode(), scope()));
    }

    String nextVarName(String name) {
        String varName = "__" + name + "__";
        var count = varNames.compute(varName, (k, v) -> v == null ? 1 : v + 1);
        return varName + count;
    }

    TryExitNode createTryExit() {
        return onNodeCreated(new TryExitNode(
                null, nextName("tryExit"),
                scope().getLastNode(),
                scope(),
                nextVariableIndex()
        ));
    }


    NonNullNode createNonNull() {
        return onNodeCreated(new NonNullNode(
                        null,
                        nextName("nonnull"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    TargetNode createTarget() {
        return onNodeCreated(new TargetNode(null, nextName("target"), scope().getLastNode(), scope()));
    }

    NewArrayNode createNewArray(ArrayType type) {
        return onNodeCreated(new NewArrayNode(
                null, nextName("NewArray"),
                type,
                scope().getLastNode(), scope()
        ));
    }

    NewArrayWithDimsNode createNewArrayWithDimensions(ArrayType type, int dimensions) {
        return onNodeCreated(new NewArrayWithDimsNode(
                null, nextName("NewArray"),
                type,
                scope().getLastNode(), scope(),
                dimensions
        ));
    }

    ArrayLengthNode createArrayLength() {
        return new ArrayLengthNode(
                null, nextName("length"),
                scope().getLastNode(),
                scope()
        );
    }

    int enterSwitchExpression() {
        var i = nextVariableIndex();
        yieldVariables.push(i);
        return i;
    }

    void exitSwitchExpression() {
        yieldVariables.pop();
    }

    int yieldVariable() {
        return Objects.requireNonNull(yieldVariables.peek());
    }

    void createYieldStore() {
        createStore(yieldVariable());
    }

    private ScopeInfo currentScope() {
        return NncUtils.requireNonNull(scopes.peek());
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

    int nextVariableIndex() {
        return scope().nextVariableIndex();
    }

    int getVariableIndex(PsiVariable variable) {
        assert !(variable instanceof PsiField);
        var i = variable.getUserData(Keys.VARIABLE_INDEX);
        if(i != null)
            return i;
        i = scope().nextVariableIndex();
        variable.putUserData(Keys.VARIABLE_INDEX, i);
        return i;
    }

    SetFieldNode createSetField(Field field) {
        return onNodeCreated(new SetFieldNode(null,
                nextName("Update"),
                scope().getLastNode(),
                scope(),
                field.getRef()));
    }

    SetStaticNode createSetStatic(Field field) {
        return onNodeCreated(new SetStaticNode(
                null, nextName("UpdateStatic"),
                scope().getLastNode(), scope(),
                field.getRef()));
    }

    AddElementNode createAddElement() {
        return onNodeCreated(
                new AddElementNode(
                        null,
                        nextName("AddElement"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    RemoveElementNode createRemoveElement() {
        return onNodeCreated(
                new RemoveElementNode(
                        null,
                        nextName("RemoveElement"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    GetElementNode createGetElement() {
        return onNodeCreated(
                new GetElementNode(
                        null,
                        nextName("GetElement"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    @SuppressWarnings("UnusedReturnValue")
    VoidReturnNode createVoidReturn() {
        return onNodeCreated(new VoidReturnNode(null,
                nextName("Exit"),
                scope().getLastNode(),
                scope()
        ));
    }

    ReturnNode createReturn() {
        return onNodeCreated(new ReturnNode(null,
                nextName("Exit"),
                scope().getLastNode(),
                scope()
        ));
    }

    IndexCountNode createIndexCount(Index index) {
        return onNodeCreated(new IndexCountNode(
                null, nextName("IndexCount"), scope().getLastNode(), scope(), index
        ));
    }

    IndexScanNode createIndexScan(Index index) {
        return onNodeCreated(new IndexScanNode(
                null, nextName("IndexScan"), scope().getLastNode(),
                scope(), index
        ));
    }

    public IndexSelectNode createIndexSelect(Index index) {
        return onNodeCreated(new IndexSelectNode(
                null, nextName("IndexSelect"),
                scope().getLastNode(), scope(), index
        ));
    }

    public IndexSelectFirstNode createIndexSelectFirst(Index index) {
        return onNodeCreated(new IndexSelectFirstNode(
                null, nextName("IndexSelectFirst"),
                scope().getLastNode(), scope(), index
        ));
    }

    public Field newTemporaryField(Klass klass, String name, Type type) {
        return FieldBuilder.newBuilder(name, klass, type).build();
    }

    MethodCallNode createMethodCall(Method method) {
        return createMethodCall(method, List.of(), List.of());
    }

    MethodCallNode createMethodCall(Method method,
                                    List<Type> capturedExpressionTypes,
                                    List<Long> capturedVariables) {
        var node = new MethodCallNode(null, nextName(method.getName()),
                scope().getLastNode(), scope(),
                method.getRef());
        node.setCapturedVariableIndexes(capturedVariables);
        node.setCapturedVariableTypes(capturedExpressionTypes);
        return onNodeCreated(node);
    }

    NodeRT createTypeCast(Type targetType) {
        targetType = Types.getNullableType(targetType);
        return createFunctionCall(StdFunction.typeCast.get().getParameterized(List.of(targetType)));
    }

    FunctionCallNode createFunctionCall(Function function) {
        var functionRef = function.getRef();
        return onNodeCreated(new FunctionCallNode(
                null,
                nextName(functionRef.getRawFlow().getName()),
                scope().getLastNode(), scope(),
                functionRef));
    }

    LambdaNode createLambda(Lambda lambda, Klass functionalInterface) {
        var node = onNodeCreated(new LambdaNode(
                null, nextName("lambda"), scope().getLastNode(), scope(),
                lambda, functionalInterface.getType()
        ));
        node.createSAMImpl();
        return node;
    }

    NewObjectNode createNew(Method method, boolean ephemeral, boolean unbound) {
        var methodRef = method.getRef();
        return onNodeCreated(new NewObjectNode(null, nextName(methodRef.resolve().getName()), methodRef,
                scope().getLastNode(), scope(), ephemeral, unbound));
    }

    ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public <T extends NodeRT> T onNodeCreated(T node) {
//        var scope = scope();
//        var lastNode = scope.getLastNode();
//        if (lastNode != null && lastNode.isSequential())
//            node.mergeExpressionTypes(lastNode.getNextExpressionTypes());
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
    public RaiseNode createRaise() {
        return onNodeCreated(new RaiseNode(
                null,
                nextName("Error"),
                scope().getLastNode(), scope()
        ));
    }

    public PsiMethod getJavaMethod() {
        return null;
    }

    public ClearArrayNode createClearArray() {
        return onNodeCreated(new ClearArrayNode(
                null, nextName("ClearArray"),
                scope().getLastNode(), scope()
        ));
    }

    public SetElementNode createSetElement() {
        return onNodeCreated(new SetElementNode(null, nextName("SetElement"),
                scope().getLastNode(), scope()));
    }

    AddNode createAdd() {
        return onNodeCreated(new AddNode(
                        null,
                        nextName("add"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    SubNode createSub() {
        return onNodeCreated(new SubNode(
                        null,
                        nextName("sub"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    MulNode createMul() {
        return onNodeCreated(new MulNode(
                        null,
                        nextName("mul"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    DivNode createDiv() {
        return onNodeCreated(new DivNode(
                        null,
                        nextName("div"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    LeftShiftNode createLeftShift() {
        return onNodeCreated(new LeftShiftNode(
                        null,
                        nextName("leftShift"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    RightShiftNode createRightShift() {
        return onNodeCreated(new RightShiftNode(
                        null,
                        nextName("rightShift"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    UnsignedRightShiftNode createUnsignedRightShift() {
        return onNodeCreated(new UnsignedRightShiftNode(
                        null,
                        nextName("unsignedRightShift"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    BitOrNode createBitOr() {
        return onNodeCreated(new BitOrNode(
                        null,
                        nextName("bitor"),
                scope().getLastNode(),
                        scope()
                )
        );
    }

    BitAndNode createBitAnd() {
        return onNodeCreated(new BitAndNode(
                        null,
                        nextName("bitand"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    BitXorNode createBitXor() {
        return onNodeCreated(new BitXorNode(
                        null,
                        nextName("bitxor"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    AndNode createAnd() {
        return onNodeCreated(new AndNode(
                        null,
                        nextName("and"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    OrNode createOr() {
        return onNodeCreated(new OrNode(
                        null,
                        nextName("or"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    RemainderNode createRem() {
        return onNodeCreated(new RemainderNode(
                        null,
                        nextName("rem"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    EqNode createEq() {
        return onNodeCreated(new EqNode(
                        null,
                        nextName("eq"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    NeNode createNe() {
        return onNodeCreated(new NeNode(
                        null,
                        nextName("ne"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    GeNode createGe() {
        return onNodeCreated(new GeNode(
                        null,
                        nextName("ge"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    GtNode createGt() {
        return onNodeCreated(new GtNode(
                        null,
                        nextName("gt"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    LtNode createLt() {
        return onNodeCreated(new LtNode(
                        null,
                        nextName("lt"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    LeNode createLe() {
        return onNodeCreated(new LeNode(
                        null,
                        nextName("le"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    InstanceOfNode createInstanceOf(Type type) {
        return onNodeCreated(new InstanceOfNode(
                        null,
                        nextName("instanceOf"),
                        scope().getLastNode(),
                        scope(),
                        type
                )
        );
    }

    BitNotNode createBitNot() {
        return onNodeCreated(new BitNotNode(
                        null,
                        nextName("bitnot"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    NotNode createNot() {
        return onNodeCreated(new NotNode(
                        null,
                        nextName("not"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    NegateNode createNegate() {
        return onNodeCreated(new NegateNode(
                        null,
                        nextName("negate"),
                        scope().getLastNode(),
                        scope()
                )
        );
    }

    GetPropertyNode createGetProperty(Property property) {
        return onNodeCreated(new GetPropertyNode(
                        null,
                        nextName("property"),
                        scope().getLastNode(),
                        scope(),
                        property.getRef()
                )
        );
    }

    GetStaticNode createGetStatic(Property property) {
        return onNodeCreated(new GetStaticNode(
                        null,
                        nextName("static"),
                        scope().getLastNode(),
                        scope(),
                        property.getRef()
                )
        );
    }

    NoopNode createNoop() {
        return new NoopNode(null, nextName("noop"), scope().getLastNode(), scope());
    }

    public NodeRT createLoadThis() {
        return createLoad(0, getThisType());
    }

    public NodeRT createLoad(int index, Type type) {
        return onNodeCreated(new LoadNode(
                null,
                nextName("load"),
                type,
                scope().getLastNode(),
                scope(),
                index
        ));
    }

    public NodeRT createStore(int index) {
        return onNodeCreated(new StoreNode(
                null,
                nextName("store"),
                scope().getLastNode(),
                scope(),
                index
        ));
    }

    public NodeRT createLoadContextSlot(int contextIndex, int slotIndex, Type type) {
        return onNodeCreated(new LoadContextSlotNode(
                null,
                nextName("store"),
                type,
                scope().getLastNode(),
                scope(),
                contextIndex,
                slotIndex
        ));
    }

    public NodeRT createStoreContextSlot(int contextIndex, int slotIndex) {
        return onNodeCreated(new StoreContextSlotNode(
                null,
                nextName("store"),
                scope().getLastNode(),
                scope(),
                contextIndex,
                slotIndex
        ));
    }

    public NodeRT createLoadConstant(org.metavm.object.instance.core.Value value) {
        return onNodeCreated(new LoadConstantNode(
                null,
                nextName("ldc"),
                scope().getLastNode(),
                scope(),
                value
        ));
    }

    public NodeRT createDup() {
        return onNodeCreated(new DupNode(
                null, nextName("dup"),
                scope().getLastNode(), scope()
        ));
    }

    public NodeRT createDupX1() {
        return onNodeCreated(new DupX1Node(
                null, nextName("dup_x1"),
                scope().getLastNode(), scope()
        ));
    }

    public NodeRT createDupX2() {
        return onNodeCreated(new DupX2Node(
                null, nextName("dup_x2"),
                scope().getLastNode(), scope()
        ));
    }

    public NodeRT createLoadType(Type type) {
        return onNodeCreated(new LoadTypeNode(
                null, nextName("loadType"),
                scope().getLastNode(), scope(), type
        ));
    }

    public void recordValue(NodeRT anchor, int variableIndex) {
        var dup = new DupNode(null, nextName("dup"), anchor, scope());
        new StoreNode(null, nextName("store"), dup, scope(), variableIndex);
    }

    public PopNode createPop() {
        return onNodeCreated(new PopNode(
                null, nextName("pop"),
                scope().getLastNode(), scope()
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
