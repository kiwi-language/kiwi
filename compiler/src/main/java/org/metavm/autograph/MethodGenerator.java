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

    IfNode createIf( @Nullable Node target) {
        return onNodeCreated(new IfNode(null,
                nextName("if"),
                code().getLastNode(),
                code(),
                target
        ));
    }

    IfNotNode createIfNot(@Nullable Node target) {
        return onNodeCreated(new IfNotNode(null,
                nextName("ifNot"),
                code().getLastNode(),
                code(),
                target
        ));
    }

    GotoNode createGoto(Node target) {
        return onNodeCreated(new GotoNode(
                null,
                 nextName("goto"),
                code().getLastNode(),
                code(),
                target
        ));
    }

    GotoNode createIncompleteGoto() {
        return onNodeCreated(new GotoNode(
                null,
                nextName("goto"),
                code().getLastNode(),
                code()
        ));
    }

    TryEnterNode createTryEnter() {
        return onNodeCreated(new TryEnterNode(null, nextName("tryEnter"), code().getLastNode(), code()));
    }

    String nextVarName(String name) {
        String varName = "__" + name + "__";
        var count = varNames.compute(varName, (k, v) -> v == null ? 1 : v + 1);
        return varName + count;
    }

    TryExitNode createTryExit() {
        return onNodeCreated(new TryExitNode(
                null, nextName("tryExit"),
                code().getLastNode(),
                code(),
                nextVariableIndex()
        ));
    }


    NonNullNode createNonNull() {
        return onNodeCreated(new NonNullNode(
                        null,
                        nextName("nonnull"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    TargetNode createTarget() {
        return onNodeCreated(new TargetNode(null, nextName("target"), code().getLastNode(), code()));
    }

    NewArrayNode createNewArray(ArrayType type) {
        return onNodeCreated(new NewArrayNode(
                null, nextName("NewArray"),
                type,
                code().getLastNode(), code()
        ));
    }

    NewArrayWithDimsNode createNewArrayWithDimensions(ArrayType type, int dimensions) {
        return onNodeCreated(new NewArrayWithDimsNode(
                null, nextName("NewArray"),
                type,
                code().getLastNode(), code(),
                dimensions
        ));
    }

    ArrayLengthNode createArrayLength() {
        return new ArrayLengthNode(
                null, nextName("length"),
                code().getLastNode(),
                code()
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

    ScopeInfo enterScope(Code code) {
        var scopeInfo = new ScopeInfo(code);
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
            var lastNode = code().getLastNode();
            if (lastNode == null) {
                return expression.getType();
            } else {
                return lastNode.getNextExpressionTypes().getType(expression);
            }
        }
    }

    Code code() {
        return currentScope().code;
    }

    int nextVariableIndex() {
        return code().nextVariableIndex();
    }

    int getVariableIndex(PsiVariable variable) {
        assert !(variable instanceof PsiField);
        var i = variable.getUserData(Keys.VARIABLE_INDEX);
        if(i != null)
            return i;
        i = code().nextVariableIndex();
        variable.putUserData(Keys.VARIABLE_INDEX, i);
        return i;
    }

    SetFieldNode createSetField(Field field) {
        return onNodeCreated(new SetFieldNode(null,
                nextName("Update"),
                code().getLastNode(),
                code(),
                field.getRef()));
    }

    SetStaticNode createSetStatic(Field field) {
        return onNodeCreated(new SetStaticNode(
                null, nextName("UpdateStatic"),
                code().getLastNode(), code(),
                field.getRef()));
    }

    AddElementNode createAddElement() {
        return onNodeCreated(
                new AddElementNode(
                        null,
                        nextName("AddElement"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    RemoveElementNode createRemoveElement() {
        return onNodeCreated(
                new RemoveElementNode(
                        null,
                        nextName("RemoveElement"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    GetElementNode createGetElement() {
        return onNodeCreated(
                new GetElementNode(
                        null,
                        nextName("GetElement"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    @SuppressWarnings("UnusedReturnValue")
    VoidReturnNode createVoidReturn() {
        return onNodeCreated(new VoidReturnNode(null,
                nextName("Exit"),
                code().getLastNode(),
                code()
        ));
    }

    ReturnNode createReturn() {
        return onNodeCreated(new ReturnNode(null,
                nextName("Exit"),
                code().getLastNode(),
                code()
        ));
    }

    IndexCountNode createIndexCount(Index index) {
        return onNodeCreated(new IndexCountNode(
                null, nextName("IndexCount"), code().getLastNode(), code(), index.getRef()
        ));
    }

    IndexScanNode createIndexScan(Index index) {
        return onNodeCreated(new IndexScanNode(
                null, nextName("IndexScan"), code().getLastNode(),
                code(), index.getRef()
        ));
    }

    public IndexSelectNode createIndexSelect(Index index) {
        return onNodeCreated(new IndexSelectNode(
                null, nextName("IndexSelect"),
                code().getLastNode(), code(), index.getRef()
        ));
    }

    public IndexSelectFirstNode createIndexSelectFirst(Index index) {
        return onNodeCreated(new IndexSelectFirstNode(
                null, nextName("IndexSelectFirst"),
                code().getLastNode(), code(), index.getRef()
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
                code().getLastNode(), code(),
                method.getRef());
        node.setCapturedVariableIndexes(capturedVariables);
        node.setCapturedVariableTypes(capturedExpressionTypes);
        return onNodeCreated(node);
    }

    Node createTypeCast(Type targetType) {
        targetType = Types.getNullableType(targetType);
        return createFunctionCall(StdFunction.typeCast.get().getParameterized(List.of(targetType)));
    }

    FunctionCallNode createFunctionCall(Function function) {
        var functionRef = function.getRef();
        return onNodeCreated(new FunctionCallNode(
                null,
                nextName(functionRef.getRawFlow().getName()),
                code().getLastNode(), code(),
                functionRef));
    }

    LambdaNode createLambda(Lambda lambda, Klass functionalInterface) {
        var node = onNodeCreated(new LambdaNode(
                null, nextName("lambda"), code().getLastNode(), code(),
                lambda, functionalInterface.getType()
        ));
        node.createSAMImpl();
        return node;
    }

    NewObjectNode createNew(Method method, boolean ephemeral, boolean unbound) {
        var methodRef = method.getRef();
        return onNodeCreated(new NewObjectNode(null, nextName(methodRef.resolve().getName()), methodRef,
                code().getLastNode(), code(), ephemeral, unbound));
    }

    ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public <T extends Node> T onNodeCreated(T node) {
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
                code().getLastNode(), code()
        ));
    }

    public PsiMethod getJavaMethod() {
        return null;
    }

    public ClearArrayNode createClearArray() {
        return onNodeCreated(new ClearArrayNode(
                null, nextName("ClearArray"),
                code().getLastNode(), code()
        ));
    }

    public SetElementNode createSetElement() {
        return onNodeCreated(new SetElementNode(null, nextName("SetElement"),
                code().getLastNode(), code()));
    }

    AddNode createAdd() {
        return onNodeCreated(new AddNode(
                        null,
                        nextName("add"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    SubNode createSub() {
        return onNodeCreated(new SubNode(
                        null,
                        nextName("sub"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    MulNode createMul() {
        return onNodeCreated(new MulNode(
                        null,
                        nextName("mul"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    DivNode createDiv() {
        return onNodeCreated(new DivNode(
                        null,
                        nextName("div"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LeftShiftNode createLeftShift() {
        return onNodeCreated(new LeftShiftNode(
                        null,
                        nextName("leftShift"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    RightShiftNode createRightShift() {
        return onNodeCreated(new RightShiftNode(
                        null,
                        nextName("rightShift"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    UnsignedRightShiftNode createUnsignedRightShift() {
        return onNodeCreated(new UnsignedRightShiftNode(
                        null,
                        nextName("unsignedRightShift"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    BitOrNode createBitOr() {
        return onNodeCreated(new BitOrNode(
                        null,
                        nextName("bitor"),
                code().getLastNode(),
                        code()
                )
        );
    }

    BitAndNode createBitAnd() {
        return onNodeCreated(new BitAndNode(
                        null,
                        nextName("bitand"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    BitXorNode createBitXor() {
        return onNodeCreated(new BitXorNode(
                        null,
                        nextName("bitxor"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    AndNode createAnd() {
        return onNodeCreated(new AndNode(
                        null,
                        nextName("and"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    OrNode createOr() {
        return onNodeCreated(new OrNode(
                        null,
                        nextName("or"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    RemainderNode createRem() {
        return onNodeCreated(new RemainderNode(
                        null,
                        nextName("rem"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    EqNode createEq() {
        return onNodeCreated(new EqNode(
                        null,
                        nextName("eq"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    NeNode createNe() {
        return onNodeCreated(new NeNode(
                        null,
                        nextName("ne"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    GeNode createGe() {
        return onNodeCreated(new GeNode(
                        null,
                        nextName("ge"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    GtNode createGt() {
        return onNodeCreated(new GtNode(
                        null,
                        nextName("gt"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LtNode createLt() {
        return onNodeCreated(new LtNode(
                        null,
                        nextName("lt"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LeNode createLe() {
        return onNodeCreated(new LeNode(
                        null,
                        nextName("le"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    InstanceOfNode createInstanceOf(Type type) {
        return onNodeCreated(new InstanceOfNode(
                        null,
                        nextName("instanceOf"),
                        code().getLastNode(),
                        code(),
                        type
                )
        );
    }

    BitNotNode createBitNot() {
        return onNodeCreated(new BitNotNode(
                        null,
                        nextName("bitnot"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    NotNode createNot() {
        return onNodeCreated(new NotNode(
                        null,
                        nextName("not"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    NegateNode createNegate() {
        return onNodeCreated(new NegateNode(
                        null,
                        nextName("negate"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    GetPropertyNode createGetProperty(Property property) {
        return onNodeCreated(new GetPropertyNode(
                        null,
                        nextName("property"),
                        code().getLastNode(),
                        code(),
                        property.getRef()
                )
        );
    }

    GetStaticNode createGetStatic(Property property) {
        return onNodeCreated(new GetStaticNode(
                        null,
                        nextName("static"),
                        code().getLastNode(),
                        code(),
                        property.getRef()
                )
        );
    }

    NoopNode createNoop() {
        return new NoopNode(null, nextName("noop"), code().getLastNode(), code());
    }

    public Node createLoadThis() {
        return createLoad(0, getThisType());
    }

    public Node createLoad(int index, Type type) {
        return onNodeCreated(new LoadNode(
                null,
                nextName("load"),
                type,
                code().getLastNode(),
                code(),
                index
        ));
    }

    public Node createStore(int index) {
        return onNodeCreated(new StoreNode(
                null,
                nextName("store"),
                code().getLastNode(),
                code(),
                index
        ));
    }

    public Node createLoadContextSlot(int contextIndex, int slotIndex, Type type) {
        return onNodeCreated(new LoadContextSlotNode(
                null,
                nextName("store"),
                type,
                code().getLastNode(),
                code(),
                contextIndex,
                slotIndex
        ));
    }

    public Node createStoreContextSlot(int contextIndex, int slotIndex) {
        return onNodeCreated(new StoreContextSlotNode(
                null,
                nextName("store"),
                code().getLastNode(),
                code(),
                contextIndex,
                slotIndex
        ));
    }

    public Node createLoadConstant(org.metavm.object.instance.core.Value value) {
        return onNodeCreated(new LoadConstantNode(
                null,
                nextName("ldc"),
                code().getLastNode(),
                code(),
                value
        ));
    }

    public Node createDup() {
        return onNodeCreated(new DupNode(
                null, nextName("dup"),
                code().getLastNode(), code()
        ));
    }

    public Node createDupX1() {
        return onNodeCreated(new DupX1Node(
                null, nextName("dup_x1"),
                code().getLastNode(), code()
        ));
    }

    public Node createDupX2() {
        return onNodeCreated(new DupX2Node(
                null, nextName("dup_x2"),
                code().getLastNode(), code()
        ));
    }

    public Node createLoadType(Type type) {
        return onNodeCreated(new LoadTypeNode(
                null, nextName("loadType"),
                code().getLastNode(), code(), type
        ));
    }

    public void recordValue(Node anchor, int variableIndex) {
        var dup = new DupNode(null, nextName("dup"), anchor, code());
        new StoreNode(null, nextName("store"), dup, code(), variableIndex);
    }

    public PopNode createPop() {
        return onNodeCreated(new PopNode(
                null, nextName("pop"),
                code().getLastNode(), code()
        ));
    }

    private record ScopeInfo(Code code) {
    }

}
