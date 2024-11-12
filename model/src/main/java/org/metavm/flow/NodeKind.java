package org.metavm.flow;

import org.metavm.flow.rest.*;
import org.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.Objects;

public enum NodeKind {
    ADD_OBJECT(NodeKindCodes.ADD_OBJECT, AddObjectNode.class, AddObjectNodeParam.class),
    SET_FIELD(NodeKindCodes.SET_FIELD, SetFieldNode.class, SetFieldNodeParam.class),
    DELETE_OBJECT(NodeKindCodes.DELETE_OBJECT, DeleteObjectNode.class, Void.class),
    RETURN(NodeKindCodes.RETURN, ReturnNode.class, Void.class),
    EXCEPTION(NodeKindCodes.EXCEPTION, RaiseNode.class, Void.class),
    METHOD_CALL(NodeKindCodes.METHOD_CALL, MethodCallNode.class, MethodCallNodeParam.class),
    GET_UNIQUE(NodeKindCodes.GET_UNIQUE, GetUniqueNode.class, GetUniqueNodeParam.class),
    NEW(NodeKindCodes.NEW, NewObjectNode.class, NewObjectNodeParam.class),
    SET_STATIC(NodeKindCodes.SET_STATIC, SetStaticNode.class, SetStaticNodeParam.class),
    NEW_ARRAY(NodeKindCodes.NEW_ARRAY, NewArrayNode.class, Void.class),
    TRY_ENTER(NodeKindCodes.TRY_ENTER, TryEnterNode.class, Void.class),
    TRY_EXIT(NodeKindCodes.TRY_EXIT, TryExitNode.class, TryExitNodeParam.class),
    FUNC(NodeKindCodes.FUNC, FunctionNode.class, FunctionNodeParam.class),
    LAMBDA(NodeKindCodes.LAMBDA, LambdaNode.class, LambdaNodeParam.class),
    ADD_ELEMENT(NodeKindCodes.ADD_ELEMENT, AddElementNode.class, Void.class),
    DELETE_ELEMENT(NodeKindCodes.DELETE_ELEMENT, RemoveElementNode.class, Void.class),
    GET_ELEMENT(NodeKindCodes.GET_ELEMENT, GetElementNode.class, Void.class),
    FUNCTION_CALL(NodeKindCodes.FUNCTION_CALL, FunctionCallNode.class, FunctionCallNodeParam.class),
    CAST_NODE(NodeKindCodes.CAST, CastNode.class, Void.class),
    CLEAR_ARRAY(NodeKindCodes.CLEAR_ARRAY, ClearArrayNode.class, Void.class),
    COPY(NodeKindCodes.COPY, CopyNode.class, Void.class),
    MAP(NodeKindCodes.MAP, MapNode.class, MapNodeParam.class),
    UNMAP(NodeKindCodes.UNMAP, UnmapNode.class, UnmapNodeParam.class),
    INDEX_SCAN(NodeKindCodes.INDEX_SCAN, IndexScanNode.class, IndexScanNodeParam.class),
    INDEX_COUNT(NodeKindCodes.INDEX_COUNT, IndexCountNode.class, IndexCountNodeParam.class),
    INDEX_SELECT(NodeKindCodes.INDEX_SELECT, IndexSelectNode.class, IndexSelectNodeParam.class),
    INDEX_SELECT_FIRST(NodeKindCodes.INDEX_SELECT_FIRST, IndexSelectFirstNode.class, IndexSelectFirstNodeParam.class),
    GOTO(NodeKindCodes.GOTO, GotoNode.class, GotoNodeParam.class),
    TARGET(NodeKindCodes.TARGET, TargetNode.class, TargetNodeParam.class),
    NON_NULL(NodeKindCodes.NON_NULL, NonNullNode.class, Void.class),
    SET_ELEMENT(NodeKindCodes.SET_ELEMENT, SetElementNode.class, Void.class),
    IF(NodeKindCodes.IF, IfNode.class, IfNodeParam.class),
    NOOP(NodeKindCodes.NOOP, NoopNode.class, Void.class),
    ADD(NodeKindCodes.ADD, AddNode.class, Void.class),
    SUB(NodeKindCodes.SUB, SubNode.class, Void.class),
    MUL(NodeKindCodes.MUL, MulNode.class, Void.class),
    DIV(NodeKindCodes.DIV, DivNode.class, Void.class),
    LEFT_SHIFT(NodeKindCodes.LEFT_SHIFT, LeftShiftNode.class, Void.class),
    RIGHT_SHIFT(NodeKindCodes.RIGHT_SHIFT, RightShiftNode.class, Void.class),
    UNSIGNED_RIGHT_SHIFT(NodeKindCodes.UNSIGNED_RIGHT_SHIFT, UnsignedRightShiftNode.class, Void.class),
    BIT_OR(NodeKindCodes.BIT_OR, BitOrNode.class, Void.class),
    BIT_AND(NodeKindCodes.BIT_AND, BitAndNode.class, Void.class),
    BIT_XOR(NodeKindCodes.BIT_XOR, BitXorNode.class, Void.class),
    AND(NodeKindCodes.AND, AndNode.class, Void.class),
    OR(NodeKindCodes.OR, OrNode.class, Void.class),
    BIT_NOT(NodeKindCodes.BIT_NOT, BitNotNode.class, Void.class),
    NOT(NodeKindCodes.NOT, NotNode.class, Void.class),
    NEGATE(NodeKindCodes.NEGATE, NegateNode.class, Void.class),
    REM(NodeKindCodes.REM, RemainderNode.class, Void.class),
    EQ(NodeKindCodes.EQ, EqNode.class, Void.class),
    NE(NodeKindCodes.NE, NeNode.class, Void.class),
    GE(NodeKindCodes.GE, GeNode.class, Void.class),
    GT(NodeKindCodes.GT, GtNode.class, Void.class),
    LT(NodeKindCodes.LT, LtNode.class, Void.class),
    LE(NodeKindCodes.LE, LeNode.class, Void.class),
    GET_PROPERTY(NodeKindCodes.GET_PROPERTY, GetPropertyNode.class, GetPropertyNodeParam.class),
    GET_STATIC(NodeKindCodes.GET_STATIC, GetStaticNode.class, GetStaticNodeParam.class),
    INSTANCE_OF(NodeKindCodes.INSTANCE_OF, InstanceOfNode.class, InstanceOfNodeParam.class),
    ARRAY_LENGTH(NodeKindCodes.ARRAY_LENGTH, ArrayLengthNode.class, Void.class),
    IF_NOT(NodeKindCodes.IF_NOT, IfNotNode.class, IfNotNodeParam.class),
    STORE(NodeKindCodes.STORE, StoreNode.class, StoreNodeParam.class),
    LOAD(NodeKindCodes.LOAD, LoadNode.class, LoadNodeParam.class),
    LOAD_CONTEXT_SLOT(NodeKindCodes.LOAD_CONTEXT_SLOT, LoadContextSlotNode.class, LoadContextSlotNodeParam.class),
    STORE_CONTEXT_SLOT(NodeKindCodes.STORE_CONTEXT_SLOT, StoreContextSlotNode.class, StoreContextSlotNodeParam.class),
    LOAD_CONSTANT(NodeKindCodes.LOAD_CONSTANT, LoadConstantNode.class, LoadConstantNodeParam.class),
    NEW_ARRAY_WITH_DIMS(NodeKindCodes.NEW_ARRAY_WITH_DIMS, NewArrayWithDimsNode.class, NewArrayWithDimsNodeParam.class),
    VOID_RETURN(NodeKindCodes.VOID_RETURN, VoidReturnNode.class, Void.class),
    LOAD_TYPE(NodeKindCodes.LOAD_TYPE, LoadTypeNode.class, LoadTypeNodeParam.class),
    DUP(NodeKindCodes.DUP, DupNode.class, Void.class),
    POP(NodeKindCodes.POP, PopNode.class, Void.class),
    DUP_X1(NodeKindCodes.DUP_X1, DupX1Node.class, Void.class),
    DUP_X2(NodeKindCodes.DUP_X2, DupX2Node.class, Void.class),
    ;

    private final int code;
    private final Class<? extends NodeRT> nodeClass;
    private final Class<?> paramClass;

    NodeKind(int code, Class<? extends NodeRT> nodeClass, Class<?> paramClass) {
        this.code = code;
        this.nodeClass = nodeClass;
        this.paramClass = paramClass;
    }

    public static NodeKind fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Node kind " + code + " not found"));
    }


    public static NodeKind fromParamClass(Class<?> paramClass) {
        return Arrays.stream(values())
                .filter(kind -> Objects.equals(kind.getParamKlass(), paramClass))
                .findAny()
                .orElseThrow(() -> new RuntimeException("NodeKind not found for param class: " + paramClass.getName()));
    }

    public static NodeKind fromNodeClass(Class<? extends NodeRT> klass) {
        return NncUtils.findRequired(values(), kind -> kind.getNodeClass().equals(klass));
    }

    public Class<?> getParamKlass() {
        return paramClass;
    }

    public Class<? extends NodeRT> getNodeClass() {
        return nodeClass;
    }

    public int code() {
        return code;
    }
}
