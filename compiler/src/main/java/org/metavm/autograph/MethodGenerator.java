package org.metavm.autograph;

import com.intellij.psi.*;
import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdMethod;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class MethodGenerator {

    private final Method method;
    private final LinkedList<ScopeInfo> scopes = new LinkedList<>();
    private final TypeResolver typeResolver;
    private final ExpressionResolver expressionResolver;
    private final Set<String> generatedNames = new HashSet<>();
    private @Nullable Section currentSection;
    private final CaptureProcessor captureProcessor;

    public MethodGenerator(Method method, TypeResolver typeResolver, VisitorBase visitor) {
        this.method = method;
        this.typeResolver = typeResolver;
        expressionResolver = new ExpressionResolver(this, typeResolver, visitor);
        captureProcessor =  new CaptureProcessor(this);
    }

    Method getMethod() {
        return method;
    }

    ClassType getThisType() {
        return method.getDeclaringType().getType();
    }

    IfNeNode createIfNe(@Nullable LabelNode target) {
        return onNodeCreated(new IfNeNode(
                nextName("if"),
                code().getLastNode(),
                code(),
                target
        ));
    }

    IfEqNode createIfEq(@Nullable LabelNode target) {
        return onNodeCreated(new IfEqNode(
                nextName("ifnot"),
                code().getLastNode(),
                code(),
                target
        ));
    }

    GotoNode createGoto(LabelNode target) {
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

    TryExitNode createTryExit() {
        return onNodeCreated(new TryExitNode(nextName("tryexit"),
                code().getLastNode(),
                code()
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

    void enterSwitchExpression(PsiSwitchExpression expression) {
        currentSection = new SwitchExpressionSection(expression, currentSection);
    }

    Section exitSection() {
        var c = Objects.requireNonNull(currentSection);
        currentSection = c.getParent();
        return c;
    }

    SwitchExpressionSection exitSwitchExpression() {
        return (SwitchExpressionSection) exitSection();
    }

    private <T> T currentSection(Class<T> klass, boolean required) {
        var s = currentSection;
        while (s != null) {
            if (klass.isInstance(s))
                return klass.cast(s);
            s = s.getParent();
        }
        if (required)
            throw new IllegalStateException("Not inside a switch section");
        else
            return null;
    }

    private ScopeInfo currentScope() {
        return Objects.requireNonNull(scopes.peek());
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

    Node createSetField(FieldRef fieldRef) {
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

    public Node createInvokeMethod(ClassType declaringType, Method method, List<Type> typeArguments) {
        var ctVars = captureProcessor.extractCapturedTypes(Utils.prepend(declaringType, typeArguments));
        var methodRef = new MethodRef(declaringType, method, typeArguments);
        if (ctVars.isEmpty())
            return createInvokeMethod(methodRef);
        else {
            captureProcessor.constructType(declaringType, this, ctVars);
            typeArguments.forEach(t -> captureProcessor.constructType(t, this, ctVars));
            return createGenericInvokeMethod(methodRef);
        }
    }

    InvokeNode createInvokeMethod(MethodRef methodRef) {
        if (methodRef.isStatic())
            return createInvokeStatic(methodRef);
        else if (methodRef.isPrivate() || methodRef.isConstructor())
            return createInvokeSpecial(methodRef);
        else
            return createInvokeVirtual(methodRef);
    }

    GenericInvokeNode createGenericInvokeMethod(MethodRef methodRef) {
        if (methodRef.isStatic())
            return createGenericInvokeStatic(methodRef);
        else if (methodRef.isPrivate() || methodRef.isConstructor())
            return createGenericInvokeSpecial(methodRef);
        else
            return createGenericInvokeVirtual(methodRef);
    }

    InvokeVirtualNode createInvokeVirtual(MethodRef methodRef) {
        return onNodeCreated(new InvokeVirtualNode(nextName("invokevirtual"),
                code().getLastNode(), code(), methodRef));
    }

    InvokeSpecialNode createInvokeSpecial(MethodRef methodRef) {
        return onNodeCreated(new InvokeSpecialNode(nextName("invokespecial"),
                code().getLastNode(), code(), methodRef));
    }

    InvokeStaticNode createInvokeStatic(MethodRef methodRef) {
        return onNodeCreated(new InvokeStaticNode(nextName("invokestatic"),
                code().getLastNode(), code(), methodRef));
    }

    GenericInvokeVirtualNode createGenericInvokeVirtual(MethodRef methodRef) {
        return onNodeCreated(new GenericInvokeVirtualNode(nextName("ginvokevirtual"),
                code().getLastNode(), code(), methodRef));
    }

    GenericInvokeSpecialNode createGenericInvokeSpecial(MethodRef methodRef) {
        return onNodeCreated(new GenericInvokeSpecialNode(nextName("ginvokespecial"),
                code().getLastNode(), code(), methodRef));
    }

    GenericInvokeStaticNode createGenericInvokeStatic(MethodRef methodRef) {
        return onNodeCreated(new GenericInvokeStaticNode(nextName("ginvokestatic"),
                code().getLastNode(), code(), methodRef));
    }

    Node createTypeCast(Type targetType) {
        targetType = Types.getNullableType(targetType);
        return createInvokeFunction(StdFunction.typeCast.get(), List.of(targetType));
    }

    public Node createInvokeFunction(Function function, List<Type> typeArguments) {
        var ctVars = captureProcessor.extractCapturedTypes(typeArguments);
        var functionRef = new FunctionRef(function, typeArguments);
        if (ctVars.isEmpty())
            return createInvokeFunction(functionRef);
        else {
            typeArguments.forEach(t -> captureProcessor.constructType(t, this, ctVars));
            return createGenericInvokeFunction(functionRef);
        }
    }

    InvokeFunctionNode createInvokeFunction(FunctionRef functionRef) {
        return onNodeCreated(new InvokeFunctionNode(
                nextName("invokefunction"),
                code().getLastNode(), code(),
                functionRef));
    }

    GenericInvokeFunctionNode createGenericInvokeFunction(FunctionRef functionRef) {
        return onNodeCreated(new GenericInvokeFunctionNode(
                nextName("ginvokefunction"),
                code().getLastNode(), code(),
                functionRef));
    }

    LambdaNode createLambda(Lambda lambda, ClassType functionalInterface) {
        return onNodeCreated(new LambdaNode(nextName("lambda"), code().getLastNode(), code(),
                lambda.getRef(), functionalInterface
        ));
    }

    NewObjectNode createNew(ClassType type, boolean ephemeral, boolean unbound) {
        return onNodeCreated(new NewObjectNode(nextName("new"), type,
                code().getLastNode(), code(), ephemeral, unbound));
    }

    ExpressionResolver getExpressionResolver() {
        return expressionResolver;
    }

    public VisitorBase getVisitor() {
        return expressionResolver.getVisitor();
    }

    public boolean isSequential() {
        var lastNode = code().getLastNode();
        return  lastNode == null || lastNode.isSequential();
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
        if (Utils.isDigits(pieces[pieces.length - 1])) {
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

    public SetElementNode createSetElement() {
        return onNodeCreated(new SetElementNode(nextName("arrayset"),
                code().getLastNode(), code()));
    }

    Node createAdd(PsiType type) {
        if(TranspileUtils.isLongType(type))
            return createLongAdd();
        else if (TranspileUtils.isIntegerType(type))
            return createIntAdd();
        else if (TranspileUtils.isDoubleType(type))
            return createDoubleAdd();
        else
            return createFloatAdd();
    }

    Node createSub(PsiType type) {
        if(TranspileUtils.isLongType(type))
            return createLongSub();
        else if(TranspileUtils.isIntegerType(type))
            return createIntSub();
        else if (TranspileUtils.isDoubleType(type))
            return createDoubleSub();
        else
            return createFloatSub();
    }

    Node createMul(PsiType type) {
        if(TranspileUtils.isLongType(type))
            return createLongMul();
        else if(TranspileUtils.isIntegerType(type))
            return createIntMul();
        else if (TranspileUtils.isDoubleType(type))
            return createDoubleMul();
        else
            return createFloatMul();
    }

    Node createDiv(PsiType type) {
        if(TranspileUtils.isLongType(type))
            return createLongDiv();
        else if(TranspileUtils.isIntegerType(type))
            return createIntDiv();
        else if (TranspileUtils.isDoubleType(type))
            return createDoubleDiv();
        else
            return createFloatDiv();
    }

    Node createRem(PsiType type) {
        if(TranspileUtils.isLongType(type))
            return createLongRem();
        else if(TranspileUtils.isIntegerType(type))
            return createIntRem();
        else if (TranspileUtils.isDoubleType(type))
            return createDoubleRem();
        else
            return createFloatRem();
    }

    Node createNeg(PsiType type) {
        if(TranspileUtils.isLongType(type))
            return createLongNeg();
        else if(TranspileUtils.isIntegerType(type))
            return createIntNeg();
        else if (TranspileUtils.isDoubleType(type))
            return createDoubleNeg();
        else
            return createFloatNeg();
    }

    Node createInc(PsiType type) {
        if(TranspileUtils.isLongType(type)) {
            createLoadConstant(Instances.longInstance(1));
            return createLongAdd();
        } else if(TranspileUtils.isIntegerType(type)) {
            createLoadConstant(Instances.intInstance(1));
            return createIntAdd();
        } else if (TranspileUtils.isDoubleType(type)){
            createLoadConstant(Instances.doubleInstance(1));
            return createDoubleAdd();
        } else {
            createLoadConstant(Instances.floatInstance(1));
            return createFloatAdd();
        }
    }

    Node createDec(PsiType type) {
        if(TranspileUtils.isLongType(type)) {
            createLoadConstant(Instances.longInstance(1));
            return createLongSub();
        } else if(TranspileUtils.isIntegerType(type)) {
            createLoadConstant(Instances.intInstance(1));
            return createIntSub();
        } else if (TranspileUtils.isDoubleType(type)) {
            createLoadConstant(Instances.doubleInstance(1));
            return createDoubleSub();
        } else {
            createLoadConstant(Instances.floatInstance(1));
            return createFloatSub();
        }
    }

    IntAddNode createIntAdd() {
        return onNodeCreated(new IntAddNode(
                        nextName("iadd"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    IntSubNode createIntSub() {
        return onNodeCreated(new IntSubNode(
                        nextName("isub"),
                        code().getLastNode(),
                        code()
                )
        );
    }
    IntMulNode createIntMul() {
        return onNodeCreated(new IntMulNode(
                        nextName("imul"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    IntDivNode createIntDiv() {
        return onNodeCreated(new IntDivNode(
                        nextName("idiv"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    IntRemNode createIntRem() {
        return onNodeCreated(new IntRemNode(
                        nextName("irem"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    IntNegNode createIntNeg() {
        return onNodeCreated(new IntNegNode(
                        nextName("ineg"),
                        code().getLastNode(),
                        code()
                )
        );
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

    FloatAddNode createFloatAdd() {
        return onNodeCreated(new FloatAddNode(
                        nextName("fadd"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    FloatSubNode createFloatSub() {
        return onNodeCreated(new FloatSubNode(
                        nextName("fsub"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    FloatMulNode createFloatMul() {
        return onNodeCreated(new FloatMulNode(
                        nextName("fmul"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    FloatDivNode createFloatDiv() {
        return onNodeCreated(new FloatDivNode(
                        nextName("fdiv"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    Node createShiftLeft(PsiType type) {
        if(TranspileUtils.isLongType(type))
            return createLongShiftLeft();
        else
            return createIntShiftLeft();
    }

    Node createShiftRight(PsiType type) {
        if(TranspileUtils.isLongType(type))
            return createLongShiftRight();
        else
            return createIntShiftRight();
    }

    Node createUnsignedShiftRight(PsiType type) {
        if(TranspileUtils.isLongType(type))
            return createLongUnsignedShiftRight();
        else
            return createIntUnsignedShiftRight();
    }

    IntShiftLeftNode createIntShiftLeft() {
        return onNodeCreated(new IntShiftLeftNode(
                        nextName("ishl"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    IntShiftRightNode createIntShiftRight() {
        return onNodeCreated(new IntShiftRightNode(
                        nextName("ishr"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    IntUnsignedShiftRightNode createIntUnsignedShiftRight() {
        return onNodeCreated(new IntUnsignedShiftRightNode(
                        nextName("iushr"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongShiftLeftNode createLongShiftLeft() {
        return onNodeCreated(new LongShiftLeftNode(
                        nextName("lshl"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongShiftRightNode createLongShiftRight() {
        return onNodeCreated(new LongShiftRightNode(
                        nextName("lshr"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongUnsignedShiftRightNode createLongUnsignedShiftRight() {
        return onNodeCreated(new LongUnsignedShiftRightNode(
                        nextName("lushr"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    Node createBitOr(PsiType type) {
        if (TranspileUtils.isLongType(type))
            return createLongBitOr();
        else
            return createIntBitOr();
    }

    Node createBitAnd(PsiType type) {
        if (TranspileUtils.isLongType(type))
            return createLongBitAnd();
        else
            return createIntBitAnd();
    }

    Node createBitXor(PsiType type) {
        if (TranspileUtils.isLongType(type))
            return createLongBitXor();
        else
            return createIntBitXor();
    }

    IntBitOrNode createIntBitOr() {
        return onNodeCreated(new IntBitOrNode(
                        nextName("ior"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    IntBitAndNode createIntBitAnd() {
        return onNodeCreated(new IntBitAndNode(
                        nextName("iand"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    IntBitXorNode createIntBitXor() {
        return onNodeCreated(new IntBitXorNode(
                        nextName("ixor"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongBitOrNode createLongBitOr() {
        return onNodeCreated(new LongBitOrNode(
                        nextName("lor"),
                code().getLastNode(),
                        code()
                )
        );
    }

    LongBitAndNode createLongBitAnd() {
        return onNodeCreated(new LongBitAndNode(
                        nextName("land"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    LongBitXorNode createLongBitXor() {
        return onNodeCreated(new LongBitXorNode(
                        nextName("lxor"),
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

    FloatRemNode createFloatRem() {
        return onNodeCreated(new FloatRemNode(
                        nextName("frem"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    Node createCompareGt(PsiType type) {
        if (TranspileUtils.isLongType(type))
            createLongCompare();
        else if (TranspileUtils.isIntegerType(type))
            createIntCompare();
        else if (TranspileUtils.isDoubleType(type))
            createDoubleCompare();
        else
            createFloatCompare();
        return createGt();
    }


    Node createCompareGe(PsiType type) {
        if (TranspileUtils.isLongType(type))
            createLongCompare();
        else if (TranspileUtils.isIntegerType(type))
            createIntCompare();
        else if (TranspileUtils.isDoubleType(type))
            createDoubleCompare();
        else
            createFloatCompare();
        return createGe();
    }

    Node createCompareLt(PsiType type) {
        if (TranspileUtils.isLongType(type))
            createLongCompare();
        else if (TranspileUtils.isIntegerType(type))
            createIntCompare();
        else if (TranspileUtils.isDoubleType(type))
            createDoubleCompare();
        else
            createFloatCompare();
        return createLt();
    }

    Node createCompareLe(PsiType type) {
        if (TranspileUtils.isLongType(type))
            createLongCompare();
        else if (TranspileUtils.isIntegerType(type))
            createIntCompare();
        else if (TranspileUtils.isDoubleType(type))
            createDoubleCompare();
        else
            createFloatCompare();
        return createLe();
    }

    Node createCompareEq(PsiType type) {
        if (type instanceof PsiPrimitiveType) {
            if (TranspileUtils.isLongType(type))
                createLongCompare();
            else if (TranspileUtils.isIntegerType(type) || TranspileUtils.isBooleanType(type))
                createIntCompare();
            else if (TranspileUtils.isDoubleType(type))
                createDoubleCompare();
            else
                createFloatCompare();
            return createEq();
        } else
          return createRefCompareEq();
    }

    Node createCompareNe(PsiType type) {
        if (type instanceof PsiPrimitiveType) {
            if (TranspileUtils.isLongType(type))
                createLongCompare();
            else if (TranspileUtils.isIntegerType(type) || TranspileUtils.isBooleanType(type))
                createIntCompare();
            else if (TranspileUtils.isDoubleType(type))
                createDoubleCompare();
            else
                createFloatCompare();
            return createNe();
        } else
            return createRefCompareNe();
    }

    IntCompareNode createIntCompare() {
        return onNodeCreated(new IntCompareNode(nextName("icmp"), code().getLastNode(), code()));
    }

    LongCompareNode createLongCompare() {
        return onNodeCreated(new LongCompareNode(nextName("lcmp"), code().getLastNode(), code()));
    }

    DoubleCompareNode createDoubleCompare() {
        return onNodeCreated(new DoubleCompareNode(nextName("dcmp"), code().getLastNode(), code()));
    }

    FloatCompareNode createFloatCompare() {
        return onNodeCreated(new FloatCompareNode(nextName("fcmp"), code().getLastNode(), code()));
    }

    RefCompareEqNode createRefCompareEq() {
        return onNodeCreated(new RefCompareEqNode(nextName("acmpeq"), code().getLastNode(), code()));
    }

    RefCompareNeNode createRefCompareNe() {
        return onNodeCreated(new RefCompareNeNode(nextName("acmpne"), code().getLastNode(), code()));
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

    Node createBitNot(PsiType type) {
        if (TranspileUtils.isLongType(type))
            return createLongBitNot();
        else
            return createIntBitNot();
    }

    Node createIntBitNot() {
        createLoadConstant(Instances.intInstance(-1));
        return createIntBitXor();
    }

    Node createLongBitNot() {
        createLoadConstant(Instances.longInstance(-1));
        return createLongBitXor();
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

    FloatNegNode createFloatNeg() {
        return onNodeCreated(new FloatNegNode(
                        nextName("fneg"),
                        code().getLastNode(),
                        code()
                )
        );
    }

    GetFieldNode createGetField(FieldRef fieldRef) {
        return onNodeCreated(new GetFieldNode(
                        nextName("getfield"),
                        code().getLastNode(),
                        code(),
                        fieldRef
                )
        );
    }

    GetMethodNode createGetMethod(MethodRef methodRef) {
        return onNodeCreated(new GetMethodNode(
                        nextName("getmethod"),
                        code().getLastNode(),
                        code(),
                        methodRef
                )
        );
    }

    GetStaticFieldNode createGetStaticField(FieldRef fieldRef) {
        return onNodeCreated(new GetStaticFieldNode(
                        nextName("getstaticfield"),
                        code().getLastNode(),
                        code(),
                        fieldRef
                )
        );
    }

    GetStaticMethodNode createGetStaticMethod(MethodRef methodRef) {
        return onNodeCreated(new GetStaticMethodNode(
                        nextName("getstaticmethod"),
                        code().getLastNode(),
                        code(),
                        methodRef
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

    public Node createLoadKlass(Type type) {
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

    public IntToLongNode createIntToLong() {
        return onNodeCreated(new IntToLongNode(nextName("i2l"), code().getLastNode(), code()));
    }

    public LongToIntNode createLongToInt() {
        return onNodeCreated(new LongToIntNode(nextName("l2i"), code().getLastNode(), code()));
    }

    public IntToDoubleNode createIntToDouble() {
        return onNodeCreated(new IntToDoubleNode(nextName("i2d"), code().getLastNode(), code()));
    }

    public DoubleToIntNode createDoubleToInt() {
        return onNodeCreated(new DoubleToIntNode(nextName("d2i"), code().getLastNode(), code()));
    }

    public Node createIntToShort() {
        return onNodeCreated(new IntToShortNode(nextName("i2s"), code().getLastNode(), code()));
    }

    public Node createIntToChar() {
        return onNodeCreated(new IntToCharNode(nextName("i2c"), code().getLastNode(), code()));
    }

    public Node createIntToByte() {
        return onNodeCreated(new IntToByteNode(nextName("i2b"), code().getLastNode(), code()));
    }

    public LoadParentNode createLoadParent(int index) {
        return new LoadParentNode(nextName("loadparent"), code().getLastNode(), code(), index);
    }

    public NewChildNode createNewChild(ClassType type) {
        return new NewChildNode(nextName("newchild"), type, code().getLastNode(), code());
    }

    public void enterBlock(PsiElement element) {
        currentSection = new Section(currentSection, element);
    }

    public Section currentSection() {
        return requireNonNull(currentSection);
    }

    public Node createFloatToInt() {
        return onNodeCreated(new FloatToIntNode(nextName("f2i"), code().getLastNode(), code()));
    }

    public Node createFloatToLong() {
        return onNodeCreated(new FloatToLongNode(nextName("f2l"), code().getLastNode(), code()));
    }

    public Node createFloatToDouble() {
        return onNodeCreated(new FloatoDoubleNode(nextName("f2d"), code().getLastNode(), code()));
    }

    public Node createIntToFloat() {
        return onNodeCreated(new IntToFloatNode(nextName("i2f"), code().getLastNode(), code()));
    }

    public Node createLongToFloat() {
        return onNodeCreated(new LongToFloatNode(nextName("l2f"), code().getLastNode(), code()));
    }

    public Node createDoubleToFloat() {
        return onNodeCreated(new DoubleToFloatNode(nextName("d2f"), code().getLastNode(), code()));
    }

    public Node createDoubleToShort() {
        createDoubleToInt();
        return createIntToShort();
    }

    public Node createDoubleToByte() {
        createDoubleToInt();
        return createIntToByte();
    }

    public Node createDoubleToChar() {
        createDoubleToInt();
        return createIntToChar();
    }

    public Node createFloatToShort() {
        createFloatToInt();
        return createIntToShort();
    }

    public Node createFloatToByte() {
        createFloatToInt();
        return createIntToByte();
    }

    public Node createFloatToChar() {
        createFloatToInt();
        return createIntToChar();
    }

    public Node createLongToShort() {
        createLongToInt();
        return createIntToShort();
    }

    public Node createLongToByte() {
        createLongToInt();
        return createIntToByte();
    }

    public Node createLongToChar() {
        createLongToInt();
        return createIntToChar();
    }

    public LabelNode createLabel() {
        return new LabelNode(nextName("label"), code().getLastNode(), code());
    }

    public void connectBranches(Collection<? extends BranchNode> branches) {
        if (!branches.isEmpty()) {
            var l = createLabel();
            branches.forEach(b -> b.setTarget(l));
        }
    }

    public TableSwitchNode createTableSwitch(int low, int high) {
        return new TableSwitchNode(nextName("tableswitch"), code().getLastNode(), code(), low, high);
    }

    public LookupSwitchNode createLookupSwitch(List<Integer> matches) {
        return new LookupSwitchNode(nextName("lookupswitch"), code().getLastNode(), code(), matches);
    }

    public LoadElementTypeNode createLoadElementType() {
        return new LoadElementTypeNode(nextName("lt_element"), code().getLastNode(), code());
    }

    public LoadUnderlyingTypeNode createLoadUnderlyingType() {
        return new LoadUnderlyingTypeNode(nextName("lt_underlying"), code().getLastNode(), code());
    }

    public LoadOwnerTypeNode createLoadOwnerType() {
        return new LoadOwnerTypeNode(nextName("lt_owner"), code().getLastNode(), code());
    }

    public LoadDeclaringTypeNode createLoadDeclaringType() {
        return new LoadDeclaringTypeNode(nextName("lt_declaringtype"), code().getLastNode(), code());
    }

    public LoadTypeArgumentNode createLoadTypeArgument(int index) {
        return new LoadTypeArgumentNode(nextName("lt_typearg"), code().getLastNode(), code(), index);
    }

    public LoadParameterTypeNode createLoadParameterType(int index) {
        return new LoadParameterTypeNode(nextName("lt_underlying"), code().getLastNode(), code(), index);
    }

    public LoadCurrentFlowNode createLoadCurrentFlow() {
        return new LoadCurrentFlowNode(nextName("lt_currentflow"), code().getLastNode(), code());
    }

    public LoadAncestorTypeNode createLoadAncestorType(ClassType type) {
        return new LoadAncestorTypeNode(nextName("lt_ancestor"), code().getLastNode(), code(), type);
    }

    public Node createLoadType(Type type) {
        return switch (type) {
            case PrimitiveType primitiveType -> createLoadPrimitiveType(primitiveType);
            case AnyType ignored -> createLoadAnyType();
            case NullType ignored -> createLoadNullType();
            case NeverType ignored -> createLoadNeverType();
            default -> createLoadConstant(type);
        };
    }

    public Node createLoadPrimitiveType(PrimitiveType primitiveType) {
        return switch (primitiveType.getKind()) {
            case BYTE -> createLoadByteType();
            case SHORT -> createLoadShortType();
            case VOID -> createLoadVoidType();
            case CHAR -> createLoadCharType();
            case INT -> createLoadIntType();
            case LONG -> createLoadLongType();
            case FLOAT -> createLoadFloatType();
            case DOUBLE -> createLoadDoubleType();
            case BOOLEAN -> createLoadBooleanType();
            case PASSWORD -> createLoadPasswordType();
            case TIME -> createLoadTimeType();
        };
    }

    public LoadReturnTypeNode createLoadReturnType() {
        return new LoadReturnTypeNode(nextName("lt_returntype"), code().getLastNode(), code());
    }

    public LoadByteTypeNode createLoadByteType() {
        return new LoadByteTypeNode(nextName("lt_byte"), code().getLastNode(), code());
    }

    public LoadShortTypeNode createLoadShortType() {
        return new LoadShortTypeNode(nextName("lt_short"), code().getLastNode(), code());
    }

    public LoadCharTypeNode createLoadCharType() {
        return new LoadCharTypeNode(nextName("lt_char"), code().getLastNode(), code());
    }

    public LoadIntTypeNode createLoadIntType() {
        return new LoadIntTypeNode(nextName("lt_int"), code().getLastNode(), code());
    }

    public LoadLongTypeNode createLoadLongType() {
        return new LoadLongTypeNode(nextName("lt_long"), code().getLastNode(), code());
    }

    public LoadFloatTypeNode createLoadFloatType() {
        return new LoadFloatTypeNode(nextName("lt_float"), code().getLastNode(), code());
    }

    public LoadDoubleTypeNode createLoadDoubleType() {
        return new LoadDoubleTypeNode(nextName("lt_double"), code().getLastNode(), code());
    }

    public LoadBooleanTypeNode createLoadBooleanType() {
        return new LoadBooleanTypeNode(nextName("lt_boolean"), code().getLastNode(), code());
    }

    public LoadStringTypeNode createLoadStringType() {
        return new LoadStringTypeNode(nextName("lt_string"), code().getLastNode(), code());
    }

    public LoadVoidTypeNode createLoadVoidType() {
        return new LoadVoidTypeNode(nextName("lt_void"), code().getLastNode(), code());
    }

    public LoadPasswordTypeNode createLoadPasswordType() {
        return new LoadPasswordTypeNode(nextName("lt_passwd"), code().getLastNode(), code());
    }

    public LoadTimeTypeNode createLoadTimeType() {
        return new LoadTimeTypeNode(nextName("lt_time"), code().getLastNode(), code());
    }

    public LoadAnyTypeNode createLoadAnyType() {
        return new LoadAnyTypeNode(nextName("lt_any"), code().getLastNode(), code());
    }

    public LoadNullTypeNode createLoadNullType() {
        return new LoadNullTypeNode(nextName("lt_null"), code().getLastNode(), code());
    }

    public LoadNeverTypeNode createLoadNeverType() {
        return new LoadNeverTypeNode(nextName("lt_never"), code().getLastNode(), code());
    }

    public LoadKlassTypeNode createLoadKlassType(KlassType type) {
        return new LoadKlassTypeNode(nextName("lt_kt"), code().getLastNode(), code(), type);
    }

    public LoadInnerKlassTypeNode createLoadInnerKlassType(KlassType type) {
        return new LoadInnerKlassTypeNode(nextName("lt_innerkt"), code().getLastNode(), code(), type);
    }

    public LoadLocalKlassTypeNode createLoadLocalKlassType(KlassType type) {
        return new LoadLocalKlassTypeNode(nextName("lt_localkt"), code().getLastNode(), code(), type);
    }

    public LoadArrayTypeNode createLoadArrayType() {
        return new LoadArrayTypeNode(nextName("lt_array"), code().getLastNode(), code());
    }

    public LoadNullableTypeNode createLoadNullableType() {
        return new LoadNullableTypeNode(nextName("lt_nullable"), code().getLastNode(), code());
    }

    public Node createLoadUnionType(int memberCount) {
        return onNodeCreated(new LoadUnionTypeNode(nextName("lt_union"),
                code().getLastNode(), code(), memberCount
        ));
    }

    public Node createLoadIntersectionType(int memberCount) {
        return onNodeCreated(new LoadIntersectionTypeNode(nextName("lt_intersect"),
                code().getLastNode(), code(), memberCount
        ));
    }

    public Node createLoadUncertainType() {
        return onNodeCreated(new LoadUncertainTypeNode(nextName("lt_uncertaint"), code().getLastNode(), code()));
    }

    public Node createLoadFunctionType(int parameterCount) {
        return onNodeCreated(new LoadFunctionTypeNode(nextName("lt_functype"), code().getLastNode(), code(), parameterCount));
    }

    public TypeOfNode createTypeOf() {
        return onNodeCreated(new TypeOfNode(nextName("typeof"), code().getLastNode(), code()));
    }

    public List<BranchNode> processCase(PsiCaseLabelElement c, int keyVar, Type keyType) {
        List<BranchNode> ifNodes;
        if (c instanceof PsiTypeTestPattern typeTestPattern)
            ifNodes = List.of(processTypeTesPattern(typeTestPattern, keyVar, keyType));
        else if (c instanceof PsiPatternGuard patternGuard) {
            ifNodes = new ArrayList<>();
            ifNodes.add(processTypeTesPattern((PsiTypeTestPattern) patternGuard.getPattern(), keyVar, keyType));
            expressionResolver.resolve(patternGuard.getGuardingExpression());
            ifNodes.add(createIfEq(null));
        } else if (keyType.getUnderlyingType().equals(Types.getStringType())) {
            createLoad(keyVar, keyType);
            expressionResolver.resolve((PsiExpression) c);
            createInvokeSpecial(StdMethod.stringEquals.get().getRef());
            ifNodes = List.of(createIfEq(null));
        } else {
            createLoad(keyVar, keyType);
            expressionResolver.resolve((PsiExpression) c);
            createRefCompareEq();
            ifNodes = List.of(createIfEq(null));
        }
        return ifNodes;
    }

    private BranchNode processTypeTesPattern(PsiTypeTestPattern typeTestPattern, int keyVar, Type keyType) {
        var checkType = requireNonNull(typeTestPattern.getCheckType()).getType();
        var patternVar = requireNonNull(typeTestPattern.getPatternVariable());
        createLoad(keyVar, keyType);
        createInstanceOf(typeResolver.resolveDeclaration(checkType));
        var ifNode = createIfEq(null);
        createLoad(keyVar, keyType);
        createStore(getVariableIndex(patternVar));
        return ifNode;
    }

    LabelNode createSwitch(PsiSwitchBlock switchBlock) {
        var keyVar = nextVariableIndex();
        var keyExpr = Objects.requireNonNull(switchBlock.getExpression());
        var statements = requireNonNull(switchBlock.getBody()).getStatements();
        var cases = new ArrayList<PsiCaseLabelElement>();
        int i = 0;
        for (PsiStatement statement : statements) {
            if (statement instanceof PsiSwitchLabelStatement label) {
                var l = label.getCaseLabelElementList();
                if (l != null) {
                    for (var c : l.getElements()) {
                        cases.add(c);
                        c.putUserData(Keys.CASE_INDEX, i++);
                    }
                }
            }
        }
        var keyType = typeResolver.resolveDeclaration(keyExpr.getType());
        var matches = getSwitchMatches(cases, keyType);
        var low = matches.isEmpty() ? 0 :matches.getFirst();
        var high = matches.isEmpty() ? -1 : matches.getLast();
        expressionResolver.resolve(keyExpr);
        createStore(keyVar);
        createLoadSwitchKey(cases, keyVar, keyType);
        var switchNode = high - low + 1 == matches.size() ?
                createTableSwitch(low, high) : createLookupSwitch(matches);
        var targets = new LabelNode[matches.size()];
        enterBlock(switchBlock);
        for (PsiStatement stmt : statements) {
            if (stmt instanceof PsiSwitchLabelStatement label) {
                var target = createLabel();
                if (label.isDefaultCase())
                    switchNode.setDefaultTarget(target);
                else {
                    var elements = requireNonNull(label.getCaseLabelElementList()).getElements();
                    for (var element : elements) {
                        var match = getSwitchMatch(element, keyType);
                        var idx = Collections.binarySearch(matches, match);
                        targets[idx] = target;
                    }
                }
            } else
                stmt.accept(expressionResolver.getVisitor());
        }
        var exit = createLabel();
        if (switchNode.getDefaultTarget() == null)
            switchNode.setDefaultTarget(exit);
        for (int j = 0; j < targets.length; j++) {
            if (targets[j] == null)
                targets[j] = switchNode.getDefaultTarget();
        }
        switchNode.setTargets(List.of(targets));
        exitSection().connectBreaks(exit);
        return exit;
    }

    private void createLoadSwitchKey(List<PsiCaseLabelElement> cases, int keyVar, Type keyType) {
        var typePatternPresent = Utils.anyMatch(cases, e -> e instanceof PsiTypeTestPattern);
        if (keyType.isInt() && !typePatternPresent) {
            createLoad(keyVar, keyType);
        } else {
            var gotoNodes = new ArrayList<GotoNode>();
            int i = 0;
            for (var c : cases) {
                var ifNodes = processCase(c, keyVar, keyType);
                createLoadConstant(Instances.intInstance(i++));
                gotoNodes.add(createGoto(null));
                var l = createLabel();
                ifNodes.forEach(n -> n.setTarget(l));
            }
            createLoadConstant(Instances.intInstance(-1));
            var l = createLabel();
            gotoNodes.forEach(g -> g.setTarget(l));
        }
    }

    private int getSwitchMatch(PsiCaseLabelElement c, Type keyType) {
        Field field;
        if (c instanceof PsiReferenceExpression refExr && refExr.resolve() instanceof PsiField psiField
                && (field = psiField.getUserData(Keys.FIELD)) != null && field.isEnumConstant())
            return field.getOrdinal();
        else if (c instanceof PsiExpression expr && TranspileUtils.isIntType(Objects.requireNonNull(expr.getType())))
            return (int) TranspileUtils.getConstant((PsiExpression) c);
        else if (keyType.isEnum() && c instanceof PsiLiteralExpression l && l.getValue() == null)
            return -1;
        else
            return requireNonNull(c.getUserData(Keys.CASE_INDEX));
    }

    private List<Integer> getSwitchMatches(List<PsiCaseLabelElement> cases, Type keyType) {
        var matches = Utils.map(cases, c -> getSwitchMatch(c, keyType));
        matches.sort(null);
        var low = matches.getFirst();
        var high = matches.getLast();
        return matches.size() < high - low + 1 >> 1 ? matches : Utils.range(low, high + 1);
    }

    public void enterTrySection(PsiCodeBlock finallyBlock) {
        currentSection = new TrySection(finallyBlock, currentSection);
    }

    public void exitTrySection() {
        exitSection();
    }

    public @Nullable TrySection currentTrySection() {
        return currentSection(TrySection.class, false);
    }

    private record ScopeInfo(Code code) {
    }

    static class SwitchExpressionSection extends Section {

        private final List<GotoNode> yields = new ArrayList<>();

        SwitchExpressionSection(PsiSwitchBlock switchBlock, @Nullable Section parent) {
            super(parent, switchBlock);
        }

        void addYield(GotoNode gotoNode) {
            yields.add(gotoNode);
        }

        void connectYields(LabelNode exit) {
            yields.forEach(g -> g.setTarget(exit));
        }

    }

}
