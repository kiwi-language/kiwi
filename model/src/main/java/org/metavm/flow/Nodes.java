package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.*;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
public class Nodes {

    public static RaiseNode raiseWithMessage(String message, Code code) {
        var type = StdKlass.runtimeException.type();
        Nodes.newObject(code, type, true, true);
        Nodes.dup(code);
        loadConstant(Instances.stringInstance(message), code);
        var constructor = type.resolveMethod(type.getName(), List.of(Types.getNullableStringType()), List.of(), false);
        invokeMethod(constructor, code);
        return raise(code);
    }

    public static RaiseNode raise(Code code) {
        return new RaiseNode(code.nextNodeName("raise"), code.getLastNode(), code
        );
    }

    public static NewArrayNode newArray(ArrayType type, Code code) {
        return new NewArrayNode(code.nextNodeName("newArray"), type, code.getLastNode(), code);
    }

    public static ArrayLengthNode arrayLength(String name, Code code) {
        return new ArrayLengthNode(name, code.getLastNode(), code);
    }

    public static GetElementNode getElement(Code code) {
        return new GetElementNode(code.nextNodeName("getElement"),
                code.getLastNode(), code);
    }

    public static NewChildNode createNewChild(Code code, ClassType type) {
        return new NewChildNode(code.nextNodeName("newchild"), type, code.getLastNode(), code);
    }

    public static NewObjectNode newObject(Code code, ClassType type, boolean ephemeral, boolean unbound) {
        return new NewObjectNode(code.nextNodeName("newObject"),
                type, code.getLastNode(), code, ephemeral, unbound);
    }

    public static VoidReturnNode voidRet(Code code) {
        return new VoidReturnNode(code.nextNodeName("voidRet"), code.getLastNode(), code);
    }

    public static ReturnNode ret(Code code) {
        return new ReturnNode(code.nextNodeName("ret"), code.getLastNode(), code);
    }

    public static AddObjectNode addObject(ClassType type, boolean ephemeral, Code code) {
        return new AddObjectNode(code.nextNodeName("addObject"), ephemeral,
                type, code.getLastNode(), code);
    }

    public static void copyArray(Supplier<Node> getSourceArray, Supplier<Node> getTargetArray, Code code) {
        forEach(getSourceArray,
                (getElement, getIndex) -> {
                    getTargetArray.get();
                    getElement.get();
                    Nodes.addElement(code);
                }, code);
    }

    public static void forEach(Supplier<Node> arraySupplier,
                               BiConsumer<Supplier<Node>, Supplier<Node>> action,
                               Code code) {
        var i = code.nextVariableIndex();
        loadConstant(Instances.intInstance(0), code);
        Nodes.store(i, code);
        var entry = label(code);
        Supplier<Node> indexSupplier = () -> Nodes.load(i, Types.getIntType(), code);
        indexSupplier.get();
        arraySupplier.get();
        arrayLength("len", code);
        Nodes.compareGe(Types.getIntType(), code);
        var ifNode = ifNe(null, code);
        Supplier<Node> elementSupplier = () -> {
            arraySupplier.get();
            indexSupplier.get();
            return Nodes.getElement(code);
        };
        action.accept(elementSupplier, indexSupplier);
        indexSupplier.get();
        loadConstant(Instances.intInstance(1), code);
        longAdd(code);
        Nodes.store(i, code);
        goto_(entry, code);
        ifNode.setTarget(label(code));
    }

    public static InvokeFunctionNode invokeFunction(Code code, Function function) {
        return new InvokeFunctionNode(code.nextNodeName("functionCall"), code.getLastNode(), code, function.getRef());
    }

    public static InvokeNode invokeMethod(MethodRef method, Code code) {
        if (method.isStatic())
            return invokeStatic(method, code);
        else if (method.isPrivate() || method.isConstructor())
            return invokeSpecial(method, code);
        else
            return invokeVirtual(method, code);
    }

    public static InvokeStaticNode invokeStatic(MethodRef method, Code code) {
        return new InvokeStaticNode(code.nextNodeName("invokestatic"), code.getLastNode(), code, method);
    }

    public static InvokeSpecialNode invokeSpecial(MethodRef method, Code code) {
        return new InvokeSpecialNode(code.nextNodeName("invokespecial"), code.getLastNode(), code, method);
    }

    public static InvokeVirtualNode invokeVirtual(MethodRef method, Code code) {
        return new InvokeVirtualNode(code.nextNodeName("invokevirtual"), code.getLastNode(), code, method);
    }

    public static FunctionNode function(Code code, FunctionType functionType) {
        return new FunctionNode(code.nextNodeName("func"), code.getLastNode(), code, functionType);
    }

    public static CastNode cast(Type outputType, Code code) {
        return new CastNode(code.nextNodeName("cast"), outputType, code.getLastNode(), code);
    }

    public static IfNeNode ifNe(@Nullable LabelNode target, Code code) {
        return new IfNeNode(
                code.nextNodeName("ifne"),
                code.getLastNode(),
                code,
                target
        );
    }

    public static IfEqNode ifEq(@Nullable LabelNode target, Code code) {
        return new IfEqNode(
                code.nextNodeName("ifeq"),
                code.getLastNode(),
                code,
                target
        );
    }

    public static GotoNode goto_(Code code) {
        return goto_(code.nextNodeName("goto"), code);
    }

    public static GotoNode goto_(String name, Code code) {
        return new GotoNode(name, code.getLastNode(), code);
    }

    public static GotoNode goto_(LabelNode target, Code code) {
        return new GotoNode(code.nextNodeName("goto"), code.getLastNode(), code, target);
    }

    public static AddElementNode addElement(Code code) {
        return new AddElementNode(code.nextNodeName("arrayadd"), code.getLastNode(), code);
    }

    public static SetElementNode setElement(Code code) {
        return new SetElementNode(code.nextNodeName("arrayset"), code.getLastNode(), code);
    }

    public static ClearArrayNode clearArray(Code code) {
        return new ClearArrayNode(code.nextNodeName("arrayclear"), code.getLastNode(), code);
    }

    public static Node setField(FieldRef fieldRef, Code code) {
        return new SetFieldNode(
                code.nextNodeName("setfield"),
                code.getLastNode(),
                code,
                fieldRef
        );
    }

    public static SetStaticNode setStatic(Field field, Code code) {
        return new SetStaticNode(
                code.nextNodeName("setStatic"),

                code.getLastNode(),
                code,
                field.getRef()
        );
    }

    public static NonNullNode nonNull(Code code) {
        return nonNull(code.nextNodeName("nonnull"), code);
    }

    public static NonNullNode nonNull(String name, Code code) {
        return new NonNullNode(name, code.getLastNode(), code);
    }

    public static NoopNode noop(Code code) {
        return new NoopNode(code.nextNodeName("noop"), code.getLastNode(), code);
    }

    public static Node add(Type type, Code code) {
        if (type.isStackInt())
            return intAdd(code);
        else if (type.isLong())
            return longAdd(code);
        else if (type.isFloat())
            return floatAdd(code);
        else
            return doubleAdd(code);
    }

    public static Node sub(Type type, Code code) {
        if (type.isStackInt())
            return intSub(code);
        else if (type.isLong())
            return longSub(code);
        else if (type.isFloat())
            return floatSub(code);
        else
            return doubleSub(code);
    }

    public static Node mul(Type type, Code code) {
        if (type.isStackInt())
            return intMul(code);
        else if (type.isLong())
            return longMul(code);
        else if (type.isFloat())
            return floatMul(code);
        else
            return doubleMul(code);
    }

    public static Node div(Type type, Code code) {
        if (type.isStackInt())
            return intDiv(code);
        else if (type.isLong())
            return longDiv(code);
        else if (type.isFloat())
            return floatDiv(code);
        else
            return doubleDiv(code);
    }

    public static Node rem(Type type, Code code) {
        if (type.isStackInt())
            return intRem(code);
        else if (type.isLong())
            return longRem(code);
        else if (type.isFloat())
            return floatRem(code);
        else
            return doubleRem(code);
    }

    public static Node intAdd(Code code) {
        return new IntAddNode(
                code.nextNodeName("iadd"),
                code.getLastNode(),
                code
        );
    }

    public static Node intSub(Code code) {
        return new IntSubNode(
                code.nextNodeName("isub"),
                code.getLastNode(),
                code
        );
    }

    public static Node intMul(Code code) {
        return new IntMulNode(
                code.nextNodeName("imul"),
                code.getLastNode(),
                code
        );
    }

    public static Node intDiv(Code code) {
        return new IntDivNode(
                code.nextNodeName("idiv"),
                code.getLastNode(),
                code
        );
    }

    public static Node intRem(Code code) {
        return new IntRemNode(
                code.nextNodeName("irem"),
                code.getLastNode(),
                code
        );
    }

    public static Node longAdd(Code code) {
        return new LongAddNode(
                code.nextNodeName("ladd"),
                code.getLastNode(),
                code
        );
    }

    public static Node longSub(Code code) {
        return new LongSubNode(
                code.nextNodeName("lsub"),
                code.getLastNode(),
                code
        );
    }

    public static Node longMul(Code code) {
        return new LongMulNode(
                code.nextNodeName("lmul"),
                code.getLastNode(),
                code
        );
    }

    public static Node longDiv(Code code) {
        return new LongDivNode(
                code.nextNodeName("ldiv"),
                code.getLastNode(),
                code
        );
    }

    public static Node longRem(Code code) {
        return new LongRemNode(
                code.nextNodeName("lrem"),
                code.getLastNode(),
                code
        );
    }

    public static Node doubleAdd(Code code) {
        return new DoubleAddNode(
                code.nextNodeName("dadd"),
                code.getLastNode(),
                code
        );
    }

    public static Node doubleSub(Code code) {
        return new DoubleSubNode(
                code.nextNodeName("dsub"),
                code.getLastNode(),
                code
        );
    }

    public static Node doubleMul(Code code) {
        return new DoubleMulNode(
                code.nextNodeName("dmul"),
                code.getLastNode(),
                code
        );
    }

    public static Node doubleDiv(Code code) {
        return new DoubleDivNode(
                code.nextNodeName("ddiv"),
                code.getLastNode(),
                code
        );
    }

    public static Node doubleRem(Code code) {
        return new DoubleRemNode(
                code.nextNodeName("drem"),
                code.getLastNode(),
                code
        );
    }


    public static Node floatAdd(Code code) {
        return new FloatAddNode(
                code.nextNodeName("fadd"),
                code.getLastNode(),
                code
        );
    }

    public static Node floatSub(Code code) {
        return new FloatSubNode(
                code.nextNodeName("fsub"),
                code.getLastNode(),
                code
        );
    }

    public static Node floatMul(Code code) {
        return new FloatMulNode(
                code.nextNodeName("fmul"),
                code.getLastNode(),
                code
        );
    }

    public static Node floatDiv(Code code) {
        return new FloatDivNode(
                code.nextNodeName("fdiv"),
                code.getLastNode(),
                code
        );
    }

    public static Node floatRem(Code code) {
        return new FloatRemNode(
                code.nextNodeName("frem"),
                code.getLastNode(),
                code
        );
    }

    public static Node shiftLeft(Type type, Code code) {
        if (type.isLong())
            return longShiftLeft(code);
        else
            return intShiftLeft(code);
    }

    public static Node shiftRight(Type type, Code code) {
        if (type.isLong())
            return longShiftRight(code);
        else
            return intShiftRight(code);
    }

    public static Node unsignedShiftRight(Type type, Code code) {
        if (type.isLong())
            return longUnsignedShiftRight(code);
        else
            return intUnsignedShiftRight(code);
    }

    public static Node intShiftLeft(Code code) {
        return new IntShiftLeftNode(
                code.nextNodeName("ishl"),
                code.getLastNode(),
                code
        );
    }

    public static Node intShiftRight(Code code) {
        return new IntShiftRightNode(
                code.nextNodeName("ishr"),
                code.getLastNode(),
                code
        );
    }

    public static Node intUnsignedShiftRight(Code code) {
        return new IntUnsignedShiftRightNode(
                code.nextNodeName("iushr"),
                code.getLastNode(),
                code
        );
    }

    public static Node longShiftLeft(Code code) {
        return new LongShiftLeftNode(
                code.nextNodeName("lshl"),
                code.getLastNode(),
                code
        );
    }

    public static Node longShiftRight(Code code) {
        return new LongShiftRightNode(
                code.nextNodeName("lshr"),
                code.getLastNode(),
                code
        );
    }

    public static Node longUnsignedShiftRight(Code code) {
        return new LongUnsignedShiftRightNode(
                code.nextNodeName("lushr"),
                code.getLastNode(),
                code
        );
    }

    public static Node intBitOr(Code code) {
        return new IntBitOrNode(
                code.nextNodeName("ior"),
                code.getLastNode(),
                code
        );
    }

    public static Node intBitAnd(Code code) {
        return new IntBitAndNode(
                code.nextNodeName("iand"),
                code.getLastNode(),
                code
        );
    }

    public static Node intBitXor(Code code) {
        return new IntBitXorNode(
                code.nextNodeName("ixor"),
                code.getLastNode(),
                code
        );
    }

    public static Node longBitOr(Code code) {
        return new LongBitOrNode(
                code.nextNodeName("lor"),
                code.getLastNode(),
                code
        );
    }

    public static Node longBitAnd(Code code) {
        return new LongBitAndNode(
                code.nextNodeName("land"),
                code.getLastNode(),
                code
        );
    }

    public static Node longBitXor(Code code) {
        return new LongBitXorNode(
                code.nextNodeName("lxor"),
                code.getLastNode(),
                code
        );
    }

    public static Node bitAnd(Type type, Code code) {
        if (type.isLong())
            return longBitAnd(code);
        else
            return intBitAnd(code);
    }

    public static Node bitOr(Type type, Code code) {
        if (type.isLong())
            return longBitOr(code);
        else
            return intBitOr(code);
    }

    public static Node bitXor(Type type, Code code) {
        if (type.isLong())
            return longBitXor(code);
        else
            return intBitXor(code);
    }

    public static Node bitNot(Type type, Code code) {
        if (type.isLong())
            return longBitNot(code);
        else
            return intBitNot(code);
    }

    public static Node intBitNot(Code code) {
        loadConstant(Instances.intInstance(-1), code);
        return intBitXor(code);
    }

    public static Node longBitNot(Code code) {
        loadConstant(Instances.longInstance(-1), code);
        return longBitXor(code);
    }

    public static Node neg(Type type, Code code) {
        if (type.isStackInt())
            return intNeg(code);
        else if(type.isLong())
            return longNeg(code);
        else if (type.isFloat())
            return floatNeg(code);
        else
            return doubleNeg(code);
    }

    public static Node intNeg(Code code) {
        return new IntNegNode(
                code.nextNodeName("ineg"),
                code.getLastNode(),
                code
        );
    }

    public static Node longNeg(Code code) {
        return new LongNegNode(
                code.nextNodeName("lneg"),
                code.getLastNode(),
                code
        );
    }

    public static Node doubleNeg(Code code) {
        return new DoubleNegNode(
                code.nextNodeName("dneg"),
                code.getLastNode(),
                code
        );
    }

    public static Node floatNeg(Code code) {
        return new FloatNegNode(
                code.nextNodeName("fneg"),
                code.getLastNode(),
                code
        );
    }

    public static Node intCompare(Code code) {
        return new IntCompareNode(code.nextNodeName("icmp"), code.getLastNode(), code);
    }

    public static Node compareEq(Type type, Code code) {
        if(type instanceof PrimitiveType primitiveType) {
            compare(type, code);
            return eq(code);
        }
        else
            return refCompareEq(code);
    }

    private static Node floatCompare(Code code) {
        return new FloatCompareNode(code.nextNodeName("fcmp"), code.getLastNode(), code);
    }

    public static Node compareNe(Type type, Code code) {
        if(type instanceof PrimitiveType) {
            compare(type, code);
            return ne(code);
        }
        else
            return refCompareNe(code);
    }

    public static Node longCompare(Code code) {
        return new LongCompareNode(code.nextNodeName("lcmp"), code.getLastNode(), code);
    }

    public static Node doubleCompare(Code code) {
        return new DoubleCompareNode(code.nextNodeName("dcmp"), code.getLastNode(), code);
    }

    public static Node refCompareEq(Code code) {
        return new RefCompareEqNode(code.nextNodeName("acmpeq"), code.getLastNode(), code);
    }

    public static Node refCompareNe(Code code) {
        return new RefCompareNeNode(code.nextNodeName("acmpne"), code.getLastNode(), code);
    }

    public static Node eq(Code code) {
        return new EqNode(
                code.nextNodeName("eq"),
                code.getLastNode(),
                code
        );
    }

    public static Node ne(Code code) {
        return new NeNode(
                code.nextNodeName("ne"),
                code.getLastNode(),
                code
        );
    }

    public static Node ge(Code code) {
        return new GeNode(
                code.nextNodeName("ge"),
                code.getLastNode(),
                code
        );
    }

    public static Node compare(Type type, Code code) {
        if(type instanceof PrimitiveType primitiveType) {
            return switch (primitiveType.getKind()) {
                case LONG -> longCompare(code);
                case INT -> intCompare(code);
                case DOUBLE -> doubleCompare(code);
                case FLOAT -> floatCompare(code);
                default -> throw new IllegalStateException("Cannot generate compare for type " + type);
            };
        }
        else
            throw new IllegalStateException("Cannot generate compare for type " + type);
    }

    public static Node compareGt(Type type, Code code) {
        compare(type, code);
        return gt(code);
    }

    public static Node compareGe(Type type, Code code) {
        compare(type, code);
        return ge(code);
    }

    public static Node compareLt(Type type, Code code) {
        compare(type, code);
        return lt(code);
    }

    public static Node compareLe(Type type, Code code) {
        compare(type, code);
        return le(code);
    }

    public static Node gt(Code code) {
        return new GtNode(
                code.nextNodeName("gt"),
                code.getLastNode(),
                code
        );
    }

    public static Node lt(Code code) {
        return new LtNode(
                code.nextNodeName("lt"),
                code.getLastNode(),
                code
        );
    }

    public static Node le(Code code) {
        return new LeNode(
                code.nextNodeName("le"),
                code.getLastNode(),
                code
        );
    }

    public static Node instanceOf(Type targetType, Code code) {
        return new InstanceOfNode(
                code.nextNodeName("le"),
                code.getLastNode(),
                code,
                targetType
        );
    }

    public static Node this_(Code code) {
        var type = ((Method) code.getFlow()).getDeclaringType().getType();
        return Nodes.load(0, type, code);
    }

    public static Node thisField(FieldRef fieldRef, Code code) {
        this_(code);
        return getField(fieldRef, code);
    }

    public static Node getField(FieldRef fieldRef, Code code) {
        return new GetFieldNode(
                code.nextNodeName("getfield"),
                code.getLastNode(),
                code,
                fieldRef
        );
    }

    public static Node getStaticField(Field field, Code code) {
        return new GetStaticFieldNode(
                code.nextNodeName("getstaticfield"),
                code.getLastNode(),
                code,
                field.getRef()
        );
    }

    public static Node store(int index, Code code) {
        return new StoreNode(
                code.nextNodeName("store"),
                code.getLastNode(),
                code,
                index
        );
    }

    public static Node argument(Callable callable, int index) {
        var i = callable instanceof Method method && !method.isStatic() ? index + 1 : index;
        return load(i, callable.getParameters().get(index).getType(), callable.getCode());
    }

    public static Node load(int index, Type type, Code code) {
        return new LoadNode(
                code.nextNodeName("load"),
                type,
                code.getLastNode(),
                code,
                index
        );
    }

    public static Node storeContextSlot(int contextIndex, int slotIndex, Code code) {
        return new StoreContextSlotNode(
                code.nextNodeName("storeContextSlot"),
                code.getLastNode(),
                code,
                contextIndex,
                slotIndex
        );
    }

    public static Node loadContextSlot(int contextIndex, int slotIndex, Type type, Code code) {
        return new LoadContextSlotNode(
                code.nextNodeName("loadContextSlot"),
                type,
                code.getLastNode(),
                code,
                contextIndex,
                slotIndex
        );
    }

    public static Node lambda(Lambda lambda, Code code) {
        return new LambdaNode(
                code.nextNodeName("lambda"),
                code.getLastNode(),
                code,
                lambda.getRef(),
                null
        );
    }

    public static Node select(Index index, Code code) {
        return new IndexSelectNode(
                code.nextNodeName("select"),
                code.getLastNode(),
                code,
                index.getRef()
        );
    }

    public static Node selectFirst(Index index, Code code) {
        return new IndexSelectFirstNode(
                code.nextNodeName("select"),
                code.getLastNode(),
                code,
                index.getRef()
        );
    }

    public static Node loadConstant(org.metavm.object.instance.core.Value value, Code code) {
        return new LoadConstantNode(
                code.nextNodeName("ldc"),
                code.getLastNode(),
                code,
                value
        );
    }

    public static Node dup(Code code) {
        return new DupNode(code.nextNodeName("dup"), code.getLastNode(), code);
    }

    public static Node dupX1(Code code) {
        return new DupX1Node(code.nextNodeName("dup_x1"), code.getLastNode(), code);
    }

    public static Node dupX2(Code code) {
        return new DupX2Node(code.nextNodeName("dup_x2"), code.getLastNode(), code);
    }

    public static Node pop(Code code) {
        return new PopNode(code.nextNodeName("pop"), code.getLastNode(), code);
    }

    public static Node longToDouble(Code code) {
        return new LongToDoubleNode(code.nextNodeName("l2d"), code.getLastNode(), code);
    }

    public static Node doubleToLong(Code code) {
        return new DoubleToLongNode(code.nextNodeName("d2l"), code.getLastNode(), code);
    }

    public static Node intToDouble(Code code) {
        return new IntToDoubleNode(code.nextNodeName("i2d"), code.getLastNode(), code);
    }

    public static Node doubleToInt(Code code) {
        return new DoubleToIntNode(code.nextNodeName("d2i"), code.getLastNode(), code);
    }

    public static Node intToLong(Code code) {
        return new IntToLongNode(code.nextNodeName("i2l"), code.getLastNode(), code);
    }

    public static Node longToInt(Code code) {
        return new LongToIntNode(code.nextNodeName("l2i"), code.getLastNode(), code);
    }

    public static Node floatToInt(Code code) {
        return new FloatToIntNode(code.nextNodeName("f2i"), code.getLastNode(), code);
    }

    public static Node floatToLong(Code code) {
        return new FloatToLongNode(code.nextNodeName("f2l"), code.getLastNode(), code);
    }

    public static Node floatToDouble(Code code) {
        return new FloatoDoubleNode(code.nextNodeName("f2d"), code.getLastNode(), code);
    }

    public static Node intToFloat(Code code) {
        return new IntToFloatNode(code.nextNodeName("i2f"), code.getLastNode(), code);
    }

    public static Node longToFloat(Code code) {
        return new LongToFloatNode(code.nextNodeName("l2f"), code.getLastNode(), code);
    }

    public static Node doubleToFloat(Code code) {
        return new DoubleToFloatNode(code.nextNodeName("d2f"), code.getLastNode(), code);
    }

    public static Node intToShort(Code code) {
        return new IntToShortNode(code.nextNodeName("i2s"), code.getLastNode(), code);
    }

    public static Node intToByte(Code code) {
        return new IntToByteNode(code.nextNodeName("i2b"), code.getLastNode(), code);
    }

    public static Node intToChar(Code code) {
        return new IntToCharNode(code.nextNodeName("i2c"), code.getLastNode(), code);
    }

    public static Node doubleToShort(Code code) {
        doubleToInt(code);
        return intToShort(code);
    }

    public static Node doubleToByte(Code code) {
        doubleToInt(code);
        return intToByte(code);
    }

    public static Node doubleToChar(Code code) {
        doubleToInt(code);
        return intToChar(code);
    }

    public static Node floatToShort(Code code) {
        floatToInt(code);
        return intToShort(code);
    }

    public static Node floatToByte(Code code) {
        floatToInt(code);
        return intToByte(code);
    }

    public static Node floatToChar(Code code) {
        floatToInt(code);
        return intToChar(code);
    }

    public static Node longToShort(Code code) {
        longToInt(code);
        return intToShort(code);
    }

    public static Node longToByte(Code code) {
        longToInt(code);
        return intToByte(code);
    }

    public static Node longToChar(Code code) {
        longToInt(code);
        return intToChar(code);
    }

    public static LabelNode label(Code code) {
        return new LabelNode(code.nextNodeName("label"), code.getLastNode(), code);
    }

}
