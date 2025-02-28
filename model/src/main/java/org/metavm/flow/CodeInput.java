package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Entity;
import org.metavm.object.instance.core.Message;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.MvInput;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class CodeInput extends MvInput  {

    private final Code code;

    private int offset;

    private @Nullable Node prev;

    private final Map<Integer, Node> offset2node = new HashMap<>();
    private final Map<Integer, LabelNode> offset2label = new HashMap<>();

    public CodeInput(Code code) {
        super(new ByteArrayInputStream(code.getCode()));
        this.code = code;
    }

    public void readNodes() {
        for (;;) {
            var code = read();
            if (code == -1) break;
            readNode(code);
        }
        offset2label.forEach((offset, label) -> {
            var node = Objects.requireNonNull(offset2node.get(offset), () -> "Cannot find node at offset " + offset);
            code.addNodeBefore(label, node);
        });
    }

    public Node readNode(int code) {
        var name = "node" + offset2node.size();
//        log.debug("Reading node {}, offset: {}, name: {}",
//                Bytecodes.getBytecodeName(code), offset, name);
        var node = switch(code) {
            case Bytecodes.GET_FIELD -> GetFieldNode.read(this, name);
            case Bytecodes.GET_METHOD -> GetMethodNode.read(this, name);
            case Bytecodes.ADD_OBJECT -> AddObjectNode.read(this, name);
            case Bytecodes.SET_FIELD -> SetFieldNode.read(this, name);
            case Bytecodes.SET_CHILD_FIELD -> SetChildFieldNode.read(this, name);
            case Bytecodes.RETURN -> ReturnNode.read(this, name);
            case Bytecodes.RAISE -> RaiseNode.read(this, name);
            case Bytecodes.INVOKE_VIRTUAL -> InvokeVirtualNode.read(this, name);
            case Bytecodes.INVOKE_SPECIAL -> InvokeSpecialNode.read(this, name);
            case Bytecodes.INVOKE_STATIC -> InvokeStaticNode.read(this, name);
            case Bytecodes.GET_UNIQUE -> GetUniqueNode.read(this, name);
            case Bytecodes.NEW -> NewObjectNode.read(this, name);
            case Bytecodes.SET_STATIC -> SetStaticNode.read(this, name);
            case Bytecodes.GENERIC_INVOKE_VIRTUAL -> GenericInvokeVirtualNode.read(this, name);
            case Bytecodes.GENERIC_INVOKE_SPECIAL -> GenericInvokeSpecialNode.read(this, name);
            case Bytecodes.GENERIC_INVOKE_STATIC -> GenericInvokeStaticNode.read(this, name);
            case Bytecodes.NEW_ARRAY -> NewArrayNode.read(this, name);
            case Bytecodes.TRY_ENTER -> TryEnterNode.read(this, name);
            case Bytecodes.TRY_EXIT -> TryExitNode.read(this, name);
            case Bytecodes.FUNC -> FunctionNode.read(this, name);
            case Bytecodes.LAMBDA -> LambdaNode.read(this, name);
            case Bytecodes.ADD_ELEMENT -> AddElementNode.read(this, name);
            case Bytecodes.DELETE_ELEMENT -> RemoveElementNode.read(this, name);
            case Bytecodes.GET_ELEMENT -> GetElementNode.read(this, name);
            case Bytecodes.INVOKE_FUNCTION -> InvokeFunctionNode.read(this, name);
            case Bytecodes.CAST -> CastNode.read(this, name);
            case Bytecodes.CLEAR_ARRAY -> ClearArrayNode.read(this, name);
            case Bytecodes.COPY -> CopyNode.read(this, name);
            case Bytecodes.INDEX_SCAN -> IndexScanNode.read(this, name);
            case Bytecodes.INDEX_COUNT -> IndexCountNode.read(this, name);
            case Bytecodes.INDEX_SELECT -> IndexSelectNode.read(this, name);
            case Bytecodes.INDEX_SELECT_FIRST -> IndexSelectFirstNode.read(this, name);
            case Bytecodes.GOTO -> GotoNode.read(this, name);
            case Bytecodes.NON_NULL -> NonNullNode.read(this, name);
            case Bytecodes.SET_ELEMENT -> SetElementNode.read(this, name);
            case Bytecodes.IF_NE -> IfNeNode.read(this, name);
            case Bytecodes.NOOP -> NoopNode.read(this, name);
            case Bytecodes.LONG_ADD -> LongAddNode.read(this, name);
            case Bytecodes.LONG_SUB -> LongSubNode.read(this, name);
            case Bytecodes.LONG_MUL -> LongMulNode.read(this, name);
            case Bytecodes.LONG_DIV -> LongDivNode.read(this, name);
            case Bytecodes.LONG_SHIFT_LEFT -> LongShiftLeftNode.read(this, name);
            case Bytecodes.LONG_SHIFT_RIGHT -> LongShiftRightNode.read(this, name);
            case Bytecodes.LONG_UNSIGNED_SHIFT_RIGHT -> LongUnsignedShiftRightNode.read(this, name);
            case Bytecodes.LONG_BIT_OR -> LongBitOrNode.read(this, name);
            case Bytecodes.LONG_BIT_AND -> LongBitAndNode.read(this, name);
            case Bytecodes.LONG_BIT_XOR -> LongBitXorNode.read(this, name);
            case Bytecodes.LONG_NEG -> LongNegNode.read(this, name);
            case Bytecodes.LONG_REM -> LongRemNode.read(this, name);
            case Bytecodes.EQ -> EqNode.read(this, name);
            case Bytecodes.NE -> NeNode.read(this, name);
            case Bytecodes.GE -> GeNode.read(this, name);
            case Bytecodes.GT -> GtNode.read(this, name);
            case Bytecodes.LT -> LoadTypeNode.read(this, name);
            case Bytecodes.LE -> LeNode.read(this, name);
            case Bytecodes.GET_STATIC_FIELD -> GetStaticFieldNode.read(this, name);
            case Bytecodes.GET_STATIC_METHOD -> GetStaticMethodNode.read(this, name);
            case Bytecodes.INSTANCE_OF -> InstanceOfNode.read(this, name);
            case Bytecodes.ARRAY_LENGTH -> ArrayLengthNode.read(this, name);
            case Bytecodes.IF_EQ -> IfEqNode.read(this, name);
            case Bytecodes.STORE -> StoreNode.read(this, name);
            case Bytecodes.LOAD -> LoadNode.read(this, name);
            case Bytecodes.LOAD_CONTEXT_SLOT -> LoadContextSlotNode.read(this, name);
            case Bytecodes.STORE_CONTEXT_SLOT -> StoreContextSlotNode.read(this, name);
            case Bytecodes.LOAD_CONSTANT -> LoadConstantNode.read(this, name);
            case Bytecodes.NEW_ARRAY_WITH_DIMS -> NewArrayWithDimsNode.read(this, name);
            case Bytecodes.VOID_RETURN -> VoidReturnNode.read(this, name);
            case Bytecodes.LOAD_KLASS -> LoadTypeNode.read(this, name);
            case Bytecodes.DUP -> DupNode.read(this, name);
            case Bytecodes.POP -> PopNode.read(this, name);
            case Bytecodes.DUP_X1 -> DupX1Node.read(this, name);
            case Bytecodes.DUP_X2 -> DupX2Node.read(this, name);
            case Bytecodes.LOAD_PARENT -> LoadParentNode.read(this, name);
            case Bytecodes.NEW_CHILD -> NewChildNode.read(this, name);
            case Bytecodes.LONG_TO_DOUBLE -> LongToDoubleNode.read(this, name);
            case Bytecodes.DOUBLE_TO_LONG -> DoubleToLongNode.read(this, name);
            case Bytecodes.DOUBLE_ADD -> DoubleAddNode.read(this, name);
            case Bytecodes.DOUBLE_SUB -> DoubleSubNode.read(this, name);
            case Bytecodes.DOUBLE_MUL -> DoubleMulNode.read(this, name);
            case Bytecodes.DOUBLE_DIV -> DoubleDivNode.read(this, name);
            case Bytecodes.DOUBLE_REM -> DoubleRemNode.read(this, name);
            case Bytecodes.DOUBLE_NEG -> DoubleNegNode.read(this, name);
            case Bytecodes.INT_TO_DOUBLE -> IntToDoubleNode.read(this, name);
            case Bytecodes.DOUBLE_TO_INT -> DoubleToIntNode.read(this, name);
            case Bytecodes.INT_TO_LONG -> IntToLongNode.read(this, name);
            case Bytecodes.LONG_TO_INT -> LongToIntNode.read(this, name);
            case Bytecodes.INT_ADD -> IntAddNode.read(this, name);
            case Bytecodes.INT_SUB -> IntSubNode.read(this, name);
            case Bytecodes.INT_MUL -> IntMulNode.read(this, name);
            case Bytecodes.INT_DIV -> IntDivNode.read(this, name);
            case Bytecodes.INT_REM -> IntRemNode.read(this, name);
            case Bytecodes.INT_NEG -> IntNegNode.read(this, name);
            case Bytecodes.INT_SHIFT_LEFT -> IntShiftLeftNode.read(this, name);
            case Bytecodes.INT_SHIFT_RIGHT -> IntShiftRightNode.read(this, name);
            case Bytecodes.INT_UNSIGNED_SHIFT_RIGHT -> IntUnsignedShiftRightNode.read(this, name);
            case Bytecodes.INT_BIT_AND -> IntBitAndNode.read(this, name);
            case Bytecodes.INT_BIT_OR -> IntBitOrNode.read(this, name);
            case Bytecodes.INT_BIT_XOR -> IntBitXorNode.read(this, name);
            case Bytecodes.LONG_COMPARE -> LongCompareNode.read(this, name);
            case Bytecodes.INT_COMPARE -> IntCompareNode.read(this, name);
            case Bytecodes.DOUBLE_COMPARE -> DoubleCompareNode.read(this, name);
            case Bytecodes.REF_COMPARE_EQ -> RefCompareEqNode.read(this, name);
            case Bytecodes.REF_COMPARE_NE -> RefCompareNeNode.read(this, name);
            case Bytecodes.FLOAT_ADD -> FloatAddNode.read(this, name);
            case Bytecodes.FLOAT_SUB -> FloatSubNode.read(this, name);
            case Bytecodes.FLOAT_MUL -> FloatMulNode.read(this, name);
            case Bytecodes.FLOAT_DIV -> FloatDivNode.read(this, name);
            case Bytecodes.FLOAT_REM -> FloatRemNode.read(this, name);
            case Bytecodes.FLOAT_NEG -> FloatNegNode.read(this, name);
            case Bytecodes.FLOAT_COMPARE -> FloatCompareNode.read(this, name);
            case Bytecodes.FLOAT_TO_INT -> FloatToIntNode.read(this, name);
            case Bytecodes.FLOAT_TO_LONG -> FloatToLongNode.read(this, name);
            case Bytecodes.FLOAT_TO_DOUBLE -> FloatoDoubleNode.read(this, name);
            case Bytecodes.INT_TO_FLOAT -> IntToFloatNode.read(this, name);
            case Bytecodes.LONG_TO_FLOAT -> LongToFloatNode.read(this, name);
            case Bytecodes.DOUBLE_TO_FLOAT -> DoubleToFloatNode.read(this, name);
            case Bytecodes.INT_TO_SHORT -> IntToShortNode.read(this, name);
            case Bytecodes.INT_TO_BYTE -> IntToByteNode.read(this, name);
            case Bytecodes.INT_TO_CHAR -> IntToCharNode.read(this, name);
            case Bytecodes.TABLE_SWITCH -> TableSwitchNode.read(this, name);
            case Bytecodes.LOOKUP_SWITCH -> LookupSwitchNode.read(this, name);
            case Bytecodes.LT_TYPE_ARGUMENT -> LoadTypeArgumentNode.read(this, name);
            case Bytecodes.LT_ELEMENT -> LoadElementTypeNode.read(this, name);
            case Bytecodes.LT_KLASS -> LoadKlassTypeNode.read(this, name);
            case Bytecodes.LT_INNER_KLASS -> LoadInnerKlassTypeNode.read(this, name);
            case Bytecodes.LT_ARRAY -> LoadArrayTypeNode.read(this, name);
            case Bytecodes.LT_BYTE -> LoadByteTypeNode.read(this, name);
            case Bytecodes.LT_SHORT -> LoadShortTypeNode.read(this, name);
            case Bytecodes.LT_CHAR -> LoadCharTypeNode.read(this, name);
            case Bytecodes.LT_INT -> LoadIntTypeNode.read(this, name);
            case Bytecodes.LT_LONG -> LoadLongTypeNode.read(this, name);
            case Bytecodes.LT_FLOAT -> LoadFloatTypeNode.read(this, name);
            case Bytecodes.LT_DOUBLE -> LoadDoubleTypeNode.read(this, name);
            case Bytecodes.LT_BOOLEAN -> LoadBooleanTypeNode.read(this, name);
            case Bytecodes.LT_STRING -> LoadStringTypeNode.read(this, name);
            case Bytecodes.LT_ANY -> LoadAnyTypeNode.read(this, name);
            case Bytecodes.LT_UNDERLYING -> LoadUnderlyingTypeNode.read(this, name);
            case Bytecodes.LT_PARAMETER_TYPE -> LoadParameterTypeNode.read(this, name);
            case Bytecodes.LT_RETURN -> LoadReturnTypeNode.read(this, name);
            case Bytecodes.LT_VOID -> LoadVoidTypeNode.read(this, name);
            case Bytecodes.LT_NULL -> LoadNullTypeNode.read(this, name);
            case Bytecodes.LT_NULLABLE -> LoadNullableTypeNode.read(this, name);
            case Bytecodes.LT_UNION -> LoadUnionTypeNode.read(this, name);
            case Bytecodes.LT_INTERSECTION -> LoadIntersectionTypeNode.read(this, name);
            case Bytecodes.LT_UNCERTAIN -> LoadUncertainTypeNode.read(this, name);
            case Bytecodes.LT_NEVER -> LoadNeverTypeNode.read(this, name);
            case Bytecodes.LT_LOCAL_KLASS -> LoadLocalKlassTypeNode.read(this, name);
            case Bytecodes.LT_FUNCTION_TYPE -> LoadFunctionTypeNode.read(this, name);
            case Bytecodes.GENERIC_INVOKE_FUNCTION -> GenericInvokeFunctionNode.read(this, name);
            case Bytecodes.TYPEOF -> TypeOfNode.read(this, name);
            case Bytecodes.LT_PASSWORD -> LoadPasswordTypeNode.read(this, name);
            case Bytecodes.LT_TIME -> LoadTimeTypeNode.read(this, name);
            case Bytecodes.LT_OWNER -> LoadOwnerTypeNode.read(this, name);
            case Bytecodes.LT_DECLARING_TYPE -> LoadDeclaringTypeNode.read(this, name);
            case Bytecodes.LT_CURRENT_FLOW -> LoadCurrentFlowNode.read(this, name);
            case Bytecodes.LT_ANCESTOR -> LoadAncestorTypeNode.read(this, name);
            default -> throw new IllegalStateException("Unrecognized bytecode: " + code);
        };
        node.setOffset(offset);
        offset2node.put(offset, node);
        offset += node.getLength();
        return prev = node;
    }

    @Nullable
    public Node getPrev() {
        return prev;
    }

    public Code getCode() {
        return code;
    }

    public Value readConstant() {
        return (Value) code.getFlow().getConstantPool().getValues()[readShort()];
    }

    public LabelNode readLabel() {
        int offset = this.offset + readShort();
        return offset2label.computeIfAbsent(offset, o -> new LabelNode("label" + offset2label.size(), null, code));
    }

    public void skipPadding() {
        var n = (offset & 3) + 3 - offset;
        for (int i = 0; i < n; i++) read();
    }

    @Override
    public Message readTree() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readRemovingInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readValueInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reference readReference() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity readEntityMessage() {
        throw new UnsupportedOperationException();
    }

}

