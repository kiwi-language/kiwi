package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expression;
import org.metavm.expression.TypeNarrower;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
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
    private final LinkedList<BlockInfo> blocks = new LinkedList<>();

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
        return onNodeCreated(new IfNode(
                nextName("if"),
                code().getLastNode(),
                code(),
                target
        ));
    }

    IfNotNode createIfNot(@Nullable Node target) {
        return onNodeCreated(new IfNotNode(
                nextName("ifnot"),
                code().getLastNode(),
                code(),
                target
        ));
    }

    GotoNode createGoto(Node target) {
        return onNodeCreated(new GotoNode(
                 nextName("goto"),
                code().getLastNode(),
                code(),
                target
        ));
    }

    GotoNode createIncompleteGoto() {
        return onNodeCreated(new GotoNode(
                nextName("goto"),
                code().getLastNode(),
                code()
        ));
    }

    TryEnterNode createTryEnter() {
        return onNodeCreated(new TryEnterNode(nextName("tryenter"), code().getLastNode(), code()));
    }

    String nextVarName(String name) {
        String varName = "__" + name + "__";
        var count = varNames.compute(varName, (k, v) -> v == null ? 1 : v + 1);
        return varName + count;
    }

    TryExitNode createTryExit() {
        return onNodeCreated(new TryExitNode(nextName("tryexit"),
                code().getLastNode(),
                code(),
                nextVariableIndex()
        ));
    }


    NonNullNode createNonNull() {
        return onNodeCreated(new NonNullNode(
                        nextName("nonnull"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    TargetNode createTarget() {
        return onNodeCreated(new TargetNode(nextName("target"), code().getLastNode(), code()));
    }

    NewArrayNode createNewArray(ArrayType type) {
        return onNodeCreated(new NewArrayNode(nextName("arraynew"),
                type,
                code().getLastNode(), code()
        ));
    }

    NewArrayWithDimsNode createNewArrayWithDimensions(ArrayType type, int dimensions) {
        return onNodeCreated(new NewArrayWithDimsNode(nextName("arraynew"),
                type,
                code().getLastNode(), code(),
                dimensions
        ));
    }

    ArrayLengthNode createArrayLength() {
        return new ArrayLengthNode(nextName("length"),
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

    SetFieldNode createSetField(FieldRef fieldRef) {
        return onNodeCreated(new SetFieldNode(
                nextName("setfield"),
                code().getLastNode(),
                code(),
                fieldRef));
    }

    SetStaticNode createSetStatic(FieldRef fieldRef) {
        return onNodeCreated(new SetStaticNode(nextName("setstatic"),
                code().getLastNode(), code(),
                fieldRef));
    }

    AddElementNode createAddElement() {
        return onNodeCreated(
                new AddElementNode(
                        nextName("arrayadd"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    RemoveElementNode createRemoveElement() {
        return onNodeCreated(
                new RemoveElementNode(
                        nextName("arrayremove"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    GetElementNode createGetElement() {
        return onNodeCreated(
                new GetElementNode(
                        nextName("arrayget"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    @SuppressWarnings("UnusedReturnValue")
    VoidReturnNode createVoidReturn() {
        return onNodeCreated(new VoidReturnNode(
                nextName("exit"),
                code().getLastNode(),
                code()
        ));
    }

    ReturnNode createReturn() {
        return onNodeCreated(new ReturnNode(
                nextName("ret"),
                code().getLastNode(),
                code()
        ));
    }

    IndexCountNode createIndexCount(Index index) {
        return onNodeCreated(new IndexCountNode(nextName("indexcount"), code().getLastNode(), code(), index.getRef()
        ));
    }

    IndexScanNode createIndexScan(Index index) {
        return onNodeCreated(new IndexScanNode(nextName("indexscan"), code().getLastNode(),
                code(), index.getRef()
        ));
    }

    public IndexSelectNode createIndexSelect(Index index) {
        return onNodeCreated(new IndexSelectNode(nextName("indexselect"),
                code().getLastNode(), code(), index.getRef()
        ));
    }

    public IndexSelectFirstNode createIndexSelectFirst(Index index) {
        return onNodeCreated(new IndexSelectFirstNode(nextName("indexget"),
                code().getLastNode(), code(), index.getRef()
        ));
    }

    MethodCallNode createMethodCall(MethodRef methodRef) {
        return createMethodCall(methodRef, List.of(), List.of());
    }

    MethodCallNode createMethodCall(MethodRef methodRef,
                                    List<Type> capturedExpressionTypes,
                                    List<Long> capturedVariables) {
        var node = new MethodCallNode(nextName(methodRef.getRawFlow().getName()),
                code().getLastNode(), code(), methodRef);
        node.setCapturedVariableIndexes(capturedVariables);
        node.setCapturedVariableTypes(capturedExpressionTypes);
        return onNodeCreated(node);
    }

    Node createTypeCast(Type targetType) {
        targetType = Types.getNullableType(targetType);
        return createFunctionCall(new FunctionRef(StdFunction.typeCast.get(), List.of(targetType)));
    }

    FunctionCallNode createFunctionCall(FunctionRef functionRef) {
        return onNodeCreated(new FunctionCallNode(
                nextName(functionRef.getRawFlow().getName()),
                code().getLastNode(), code(),
                functionRef));
    }

    LambdaNode createLambda(Lambda lambda, ClassType functionalInterface) {
        return onNodeCreated(new LambdaNode(nextName("lambda"), code().getLastNode(), code(),
                lambda, functionalInterface
        ));
    }

    NewObjectNode createNew(MethodRef methodRef, boolean ephemeral, boolean unbound) {
        return onNodeCreated(new NewObjectNode(nextName(methodRef.getRawFlow().getName()), methodRef,
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
                nextName("raise"),
                code().getLastNode(), code()
        ));
    }

    public PsiMethod getJavaMethod() {
        return null;
    }

    public ClearArrayNode createClearArray() {
        return onNodeCreated(new ClearArrayNode(nextName("arrayclear"),
                code().getLastNode(), code()
        ));
    }

    public SetElementNode createSetElement() {
        return onNodeCreated(new SetElementNode(nextName("arrayset"),
                code().getLastNode(), code()));
    }

    Node createAdd(PsiType type) {
        if(TranspileUtils.isIntegerType(type))
            return createLongAdd();
        else
            return createDoubleAdd();
    }

    Node createSub(PsiType type) {
        if(TranspileUtils.isIntegerType(type))
            return createLongSub();
        else
            return createDoubleSub();
    }

    Node createMul(PsiType type) {
        if(TranspileUtils.isIntegerType(type))
            return createLongMul();
        else
            return createDoubleMul();
    }

    Node createDiv(PsiType type) {
        if(TranspileUtils.isIntegerType(type))
            return createLongDiv();
        else
            return createDoubleDiv();
    }

    Node createRem(PsiType type) {
        if(TranspileUtils.isIntegerType(type))
            return createLongRem();
        else
            return createDoubleRem();
    }

    Node createNeg(PsiType type) {
        if(TranspileUtils.isIntegerType(type))
            return createLongNeg();
        else
            return createDoubleNeg();
    }

    Node createInc(PsiType type) {
        if(TranspileUtils.isIntegerType(type)) {
            createLoadConstant(Instances.longInstance(1));
            return createLongAdd();
        }
        else {
            createLoadConstant(Instances.doubleInstance(1));
            return createDoubleAdd();
        }
    }

    Node createDec(PsiType type) {
        if(TranspileUtils.isIntegerType(type)) {
            createLoadConstant(Instances.longInstance(1));
            return createLongSub();
        }
        else {
            createLoadConstant(Instances.doubleInstance(1));
            return createDoubleSub();
        }
    }

    LongAddNode createLongAdd() {
        return onNodeCreated(new LongAddNode(
                        nextName("ladd"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongSubNode createLongSub() {
        return onNodeCreated(new LongSubNode(
                        nextName("lsub"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongMulNode createLongMul() {
        return onNodeCreated(new LongMulNode(
                        nextName("lmul"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongDivNode createLongDiv() {
        return onNodeCreated(new LongDivNode(
                        nextName("ldiv"),
                        code().getLastNode(),
                        code()
                )
        );
    }


    DoubleAddNode createDoubleAdd() {
        return onNodeCreated(new DoubleAddNode(
                        nextName("dadd"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    DoubleSubNode createDoubleSub() {
        return onNodeCreated(new DoubleSubNode(
                        nextName("dsub"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    DoubleMulNode createDoubleMul() {
        return onNodeCreated(new DoubleMulNode(
                        nextName("dmul"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    DoubleDivNode createDoubleDiv() {
        return onNodeCreated(new DoubleDivNode(
                        nextName("ddiv"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LeftShiftNode createLeftShift() {
        return onNodeCreated(new LeftShiftNode(
                        nextName("lshift"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    RightShiftNode createRightShift() {
        return onNodeCreated(new RightShiftNode(
                        nextName("rshift"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    UnsignedRightShiftNode createUnsignedRightShift() {
        return onNodeCreated(new UnsignedRightShiftNode(
                        nextName("urshift"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    BitOrNode createBitOr() {
        return onNodeCreated(new BitOrNode(
                        nextName("bitor"),
                code().getLastNode(),
                        code()
                )
        );
    }

    BitAndNode createBitAnd() {
        return onNodeCreated(new BitAndNode(
                        nextName("bitand"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    BitXorNode createBitXor() {
        return onNodeCreated(new BitXorNode(
                        nextName("bitxor"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    AndNode createAnd() {
        return onNodeCreated(new AndNode(
                        nextName("and"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    OrNode createOr() {
        return onNodeCreated(new OrNode(
                        nextName("or"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongRemNode createLongRem() {
        return onNodeCreated(new LongRemNode(
                        nextName("lrem"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    DoubleRemNode createDoubleRem() {
        return onNodeCreated(new DoubleRemNode(
                        nextName("drem"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    EqNode createEq() {
        return onNodeCreated(new EqNode(
                        nextName("eq"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    NeNode createNe() {
        return onNodeCreated(new NeNode(
                        nextName("ne"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    GeNode createGe() {
        return onNodeCreated(new GeNode(
                        nextName("ge"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    GtNode createGt() {
        return onNodeCreated(new GtNode(
                        nextName("gt"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LtNode createLt() {
        return onNodeCreated(new LtNode(
                        nextName("lt"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LeNode createLe() {
        return onNodeCreated(new LeNode(
                        nextName("le"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    InstanceOfNode createInstanceOf(Type type) {
        return onNodeCreated(new InstanceOfNode(
                        nextName("instanceof"),
                        code().getLastNode(),
                        code(),
                        type
                )
        );
    }

    BitNotNode createBitNot() {
        return onNodeCreated(new BitNotNode(
                        nextName("bitnot"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    NotNode createNot() {
        return onNodeCreated(new NotNode(
                        nextName("not"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongNegNode createLongNeg() {
        return onNodeCreated(new LongNegNode(
                        nextName("lneg"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    DoubleNegNode createDoubleNeg() {
        return onNodeCreated(new DoubleNegNode(
                        nextName("dneg"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    GetPropertyNode createGetProperty(PropertyRef propertyRef) {
        return onNodeCreated(new GetPropertyNode(
                        nextName("property"),
                        code().getLastNode(),
                        code(),
                        propertyRef
                )
        );
    }

    GetStaticNode createGetStatic(PropertyRef propertyRef) {
        return onNodeCreated(new GetStaticNode(
                        nextName("static"),
                        code().getLastNode(),
                        code(),
                        propertyRef
                )
        );
    }

    NoopNode createNoop() {
        return new NoopNode(nextName("noop"), code().getLastNode(), code());
    }

    public Node createLoadThis() {
        return createLoad(0, getThisType());
    }

    public Node createLoad(int index, Type type) {
        return onNodeCreated(new LoadNode(
                nextName("load"),
                type,
                code().getLastNode(),
                code(),
                index
        ));
    }

    public Node createStore(int index) {
        return onNodeCreated(new StoreNode(
                nextName("store"),
                code().getLastNode(),
                code(),
                index
        ));
    }

    public Node createLoadContextSlot(int contextIndex, int slotIndex, Type type) {
        return onNodeCreated(new LoadContextSlotNode(
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
                nextName("store"),
                code().getLastNode(),
                code(),
                contextIndex,
                slotIndex
        ));
    }

    public Node createLoadConstant(org.metavm.object.instance.core.Value value) {
        return onNodeCreated(new LoadConstantNode(
                nextName("ldc"),
                code().getLastNode(),
                code(),
                value
        ));
    }

    public Node createDup() {
        return onNodeCreated(new DupNode(nextName("dup"),
                code().getLastNode(), code()
        ));
    }

    public Node createDupX1() {
        return onNodeCreated(new DupX1Node(nextName("dup_x1"),
                code().getLastNode(), code()
        ));
    }

    public Node createDupX2() {
        return onNodeCreated(new DupX2Node(nextName("dup_x2"),
                code().getLastNode(), code()
        ));
    }

    public Node createLoadType(Type type) {
        return onNodeCreated(new LoadTypeNode(nextName("loadtype"),
                code().getLastNode(), code(), type
        ));
    }

    public void recordValue(Node anchor, int variableIndex) {
        var dup = new DupNode(nextName("dup"), anchor, code());
        new StoreNode(nextName("store"), dup, code(), variableIndex);
    }

    public PopNode createPop() {
        return onNodeCreated(new PopNode(nextName("pop"),
                code().getLastNode(), code()
        ));
    }

    public LongToDoubleNode createLongToDouble() {
        return onNodeCreated(new LongToDoubleNode(nextName("l2d"), code().getLastNode(), code()));
    }

    public DoubleToLongNode createDoubleToLong() {
        return onNodeCreated(new DoubleToLongNode(nextName("d2l"), code().getLastNode(), code()));
    }

    public LoadParentNode createLoadParent(int index) {
        return new LoadParentNode(nextName("loadparent"), code().getLastNode(), code(), index);
    }

    public NewChildNode createNewChild(MethodRef methodRef) {
        return new NewChildNode(nextName("newchild"), methodRef, code().getLastNode(), code());
    }

    public void enterBlock(PsiStatement statement) {
        blocks.push(new BlockInfo(blocks.peek(), statement));
    }

    public BlockInfo exitBlock() {
        return Objects.requireNonNull(blocks.pop());
    }

    public BlockInfo currentBlock() {
        return Objects.requireNonNull(blocks.peek());
    }

    private record ScopeInfo(Code code) {
    }

}
