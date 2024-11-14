package org.metavm.flow;

import org.metavm.flow.rest.*;
import org.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.Objects;

public enum NodeKind {
    ADD_OBJECT(Bytecodes.ADD_OBJECT, AddObjectNode.class, AddObjectNodeParam.class),
    SET_FIELD(Bytecodes.SET_FIELD, SetFieldNode.class, SetFieldNodeParam.class),
    DELETE_OBJECT(Bytecodes.DELETE_OBJECT, DeleteObjectNode.class, Void.class),
    RETURN(Bytecodes.RETURN, ReturnNode.class, Void.class),
    RAISE(Bytecodes.RAISE, RaiseNode.class, Void.class),
    METHOD_CALL(Bytecodes.METHOD_CALL, MethodCallNode.class, MethodCallNodeParam.class),
    GET_UNIQUE(Bytecodes.GET_UNIQUE, GetUniqueNode.class, GetUniqueNodeParam.class),
    NEW(Bytecodes.NEW, NewObjectNode.class, NewObjectNodeParam.class),
    SET_STATIC(Bytecodes.SET_STATIC, SetStaticNode.class, SetStaticNodeParam.class),
    NEW_ARRAY(Bytecodes.NEW_ARRAY, NewArrayNode.class, Void.class),
    TRY_ENTER(Bytecodes.TRY_ENTER, TryEnterNode.class, Void.class),
    TRY_EXIT(Bytecodes.TRY_EXIT, TryExitNode.class, TryExitNodeParam.class),
    FUNC(Bytecodes.FUNC, FunctionNode.class, FunctionNodeParam.class),
    LAMBDA(Bytecodes.LAMBDA, LambdaNode.class, LambdaNodeParam.class),
    ADD_ELEMENT(Bytecodes.ADD_ELEMENT, AddElementNode.class, Void.class),
    DELETE_ELEMENT(Bytecodes.DELETE_ELEMENT, RemoveElementNode.class, Void.class),
    GET_ELEMENT(Bytecodes.GET_ELEMENT, GetElementNode.class, Void.class),
    FUNCTION_CALL(Bytecodes.FUNCTION_CALL, FunctionCallNode.class, FunctionCallNodeParam.class),
    CAST_NODE(Bytecodes.CAST, CastNode.class, Void.class),
    CLEAR_ARRAY(Bytecodes.CLEAR_ARRAY, ClearArrayNode.class, Void.class),
    COPY(Bytecodes.COPY, CopyNode.class, Void.class),
    MAP(Bytecodes.MAP, MapNode.class, MapNodeParam.class),
    UNMAP(Bytecodes.UNMAP, UnmapNode.class, UnmapNodeParam.class),
    INDEX_SCAN(Bytecodes.INDEX_SCAN, IndexScanNode.class, IndexScanNodeParam.class),
    INDEX_COUNT(Bytecodes.INDEX_COUNT, IndexCountNode.class, IndexCountNodeParam.class),
    INDEX_SELECT(Bytecodes.INDEX_SELECT, IndexSelectNode.class, IndexSelectNodeParam.class),
    INDEX_SELECT_FIRST(Bytecodes.INDEX_SELECT_FIRST, IndexSelectFirstNode.class, IndexSelectFirstNodeParam.class),
    GOTO(Bytecodes.GOTO, GotoNode.class, GotoNodeParam.class),
    TARGET(Bytecodes.TARGET, TargetNode.class, TargetNodeParam.class),
    NON_NULL(Bytecodes.NON_NULL, NonNullNode.class, Void.class),
    SET_ELEMENT(Bytecodes.SET_ELEMENT, SetElementNode.class, Void.class),
    IF(Bytecodes.IF, IfNode.class, IfNodeParam.class),
    NOOP(Bytecodes.NOOP, NoopNode.class, Void.class),
    ADD(Bytecodes.ADD, AddNode.class, Void.class),
    SUB(Bytecodes.SUB, SubNode.class, Void.class),
    MUL(Bytecodes.MUL, MulNode.class, Void.class),
    DIV(Bytecodes.DIV, DivNode.class, Void.class),
    LEFT_SHIFT(Bytecodes.LEFT_SHIFT, LeftShiftNode.class, Void.class),
    RIGHT_SHIFT(Bytecodes.RIGHT_SHIFT, RightShiftNode.class, Void.class),
    UNSIGNED_RIGHT_SHIFT(Bytecodes.UNSIGNED_RIGHT_SHIFT, UnsignedRightShiftNode.class, Void.class),
    BIT_OR(Bytecodes.BIT_OR, BitOrNode.class, Void.class),
    BIT_AND(Bytecodes.BIT_AND, BitAndNode.class, Void.class),
    BIT_XOR(Bytecodes.BIT_XOR, BitXorNode.class, Void.class),
    AND(Bytecodes.AND, AndNode.class, Void.class),
    OR(Bytecodes.OR, OrNode.class, Void.class),
    BIT_NOT(Bytecodes.BIT_NOT, BitNotNode.class, Void.class),
    NOT(Bytecodes.NOT, NotNode.class, Void.class),
    NEGATE(Bytecodes.NEGATE, NegateNode.class, Void.class),
    REM(Bytecodes.REM, RemainderNode.class, Void.class),
    EQ(Bytecodes.EQ, EqNode.class, Void.class),
    NE(Bytecodes.NE, NeNode.class, Void.class),
    GE(Bytecodes.GE, GeNode.class, Void.class),
    GT(Bytecodes.GT, GtNode.class, Void.class),
    LT(Bytecodes.LT, LtNode.class, Void.class),
    LE(Bytecodes.LE, LeNode.class, Void.class),
    GET_PROPERTY(Bytecodes.GET_PROPERTY, GetPropertyNode.class, GetPropertyNodeParam.class),
    GET_STATIC(Bytecodes.GET_STATIC, GetStaticNode.class, GetStaticNodeParam.class),
    INSTANCE_OF(Bytecodes.INSTANCE_OF, InstanceOfNode.class, InstanceOfNodeParam.class),
    ARRAY_LENGTH(Bytecodes.ARRAY_LENGTH, ArrayLengthNode.class, Void.class),
    IF_NOT(Bytecodes.IF_NOT, IfNotNode.class, IfNotNodeParam.class),
    STORE(Bytecodes.STORE, StoreNode.class, StoreNodeParam.class),
    LOAD(Bytecodes.LOAD, LoadNode.class, LoadNodeParam.class),
    LOAD_CONTEXT_SLOT(Bytecodes.LOAD_CONTEXT_SLOT, LoadContextSlotNode.class, LoadContextSlotNodeParam.class),
    STORE_CONTEXT_SLOT(Bytecodes.STORE_CONTEXT_SLOT, StoreContextSlotNode.class, StoreContextSlotNodeParam.class),
    LOAD_CONSTANT(Bytecodes.LOAD_CONSTANT, LoadConstantNode.class, LoadConstantNodeParam.class),
    NEW_ARRAY_WITH_DIMS(Bytecodes.NEW_ARRAY_WITH_DIMS, NewArrayWithDimsNode.class, NewArrayWithDimsNodeParam.class),
    VOID_RETURN(Bytecodes.VOID_RETURN, VoidReturnNode.class, Void.class),
    LOAD_TYPE(Bytecodes.LOAD_TYPE, LoadTypeNode.class, LoadTypeNodeParam.class),
    DUP(Bytecodes.DUP, DupNode.class, Void.class),
    POP(Bytecodes.POP, PopNode.class, Void.class),
    DUP_X1(Bytecodes.DUP_X1, DupX1Node.class, Void.class),
    DUP_X2(Bytecodes.DUP_X2, DupX2Node.class, Void.class),
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
