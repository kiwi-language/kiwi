package org.metavm.flow;

import org.metavm.flow.rest.*;
import org.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public enum NodeKind {
    ADD_OBJECT(NodeKindCodes.ADD_OBJECT, AddObjectNode.class, AddObjectNodeParam.class),
    SET_FIELD(NodeKindCodes.SET_FIELD, SetFieldNode.class, SetFieldNodeParam.class),
    DELETE_OBJECT(NodeKindCodes.DELETE_OBJECT, DeleteObjectNode.class, DeleteObjectNodeParam.class),
    RETURN(NodeKindCodes.RETURN, ReturnNode.class, ReturnNodeParam.class),
    EXCEPTION(NodeKindCodes.EXCEPTION, RaiseNode.class, RaiseNodeParam.class),
    METHOD_CALL(NodeKindCodes.METHOD_CALL, MethodCallNode.class, MethodCallNodeParam.class),
    GET_UNIQUE(NodeKindCodes.GET_UNIQUE, GetUniqueNode.class, GetUniqueNodeParam.class),
    NEW(NodeKindCodes.NEW, NewObjectNode.class, NewObjectNodeParam.class),
    VALUE(NodeKindCodes.VALUE, ValueNode.class, ValueNodeParam.class),
    SET_STATIC(NodeKindCodes.SET_STATIC, SetStaticNode.class, SetStaticNodeParam.class),
    NEW_ARRAY(NodeKindCodes.NEW_ARRAY, NewArrayNode.class, NewArrayNodeParam.class),
    TRY_ENTER(NodeKindCodes.TRY_ENTER, TryEnterNode.class, TryEnterNodeParam.class),
    TRY_EXIT(NodeKindCodes.TRY_EXIT, TryExitNode.class, TryExitNodeParam.class, true),
    FUNC(NodeKindCodes.FUNC, FunctionNode.class, FunctionNodeParam.class),
    LAMBDA(NodeKindCodes.LAMBDA, LambdaNode.class, LambdaNodeParam.class),
    ADD_ELEMENT(NodeKindCodes.ADD_ELEMENT, AddElementNode.class, AddElementNodeParam.class),
    DELETE_ELEMENT(NodeKindCodes.DELETE_ELEMENT, RemoveElementNode.class, RemoveElementNodeParam.class),
    GET_ELEMENT(NodeKindCodes.GET_ELEMENT, GetElementNode.class, GetElementNodeParam.class),
    FUNCTION_CALL(NodeKindCodes.FUNCTION_CALL, FunctionCallNode.class, FunctionCallNodeParam.class),
    CAST_NODE(NodeKindCodes.CAST, CastNode.class, CastNodeParam.class),
    CLEAR_ARRAY(NodeKindCodes.CLEAR_ARRAY, ClearArrayNode.class, ClearArrayNodeParam.class),
    COPY(NodeKindCodes.COPY, CopyNode.class, CopyNodeParam.class),
    MAP(NodeKindCodes.MAP, MapNode.class, MapNodeParam.class),
    UNMAP(NodeKindCodes.UNMAP, UnmapNode.class, UnmapNodeParam.class),
    INDEX_SCAN(NodeKindCodes.INDEX_SCAN, IndexScanNode.class, IndexScanNodeParam.class),
    INDEX_COUNT(NodeKindCodes.INDEX_COUNT, IndexCountNode.class, IndexCountNodeParam.class),
    INDEX_SELECT(NodeKindCodes.INDEX_SELECT, IndexSelectNode.class, IndexSelectNodeParam.class),
    INDEX_SELECT_FIRST(NodeKindCodes.INDEX_SELECT_FIRST, IndexSelectFirstNode.class, IndexSelectFirstNodeParam.class),
    GOTO(NodeKindCodes.GOTO, GotoNode.class, GotoNodeParam.class, false),
    TARGET(NodeKindCodes.TARGET, TargetNode.class, TargetNodeParam.class, false),
    NON_NULL(NodeKindCodes.NON_NULL, NonNullNode.class, NonNullNodeParam.class),
    SET_ELEMENT(NodeKindCodes.SET_ELEMENT, SetElementNode.class, SetElementNodeParam.class),
    IF(NodeKindCodes.IF, IfNode.class, IfNodeParam.class, false),
    JOIN(NodeKindCodes.JOIN, JoinNode.class, JoinNodeParam.class, true),
    NOOP(NodeKindCodes.NOOP, NoopNode.class, NoopNodeParam.class),
    ADD(NodeKindCodes.ADD, AddNode.class, AddNodeParam.class),
    SUB(NodeKindCodes.SUB, SubNode.class, SubNodeParam.class),
    MUL(NodeKindCodes.MUL, MultiplyNode.class, MultiplyNodeParam.class),
    DIV(NodeKindCodes.DIV, DivideNode.class, DivideNodeParam.class),
    LEFT_SHIFT(NodeKindCodes.LEFT_SHIFT, LeftShiftNode.class, LeftShiftNodeParam.class),
    RIGHT_SHIFT(NodeKindCodes.RIGHT_SHIFT, RightShiftNode.class, RightShiftNodeParam.class),
    UNSIGNED_RIGHT_SHIFT(NodeKindCodes.UNSIGNED_RIGHT_SHIFT, UnsignedRightShiftNode.class, UnsignedRightShiftNodeParam.class),
    BITWISE_OR(NodeKindCodes.BITWISE_OR, BitwiseOrNode.class, BitwiseOrNodeParam.class),
    BITWISE_AND(NodeKindCodes.BITWISE_AND, BitwiseAndNode.class, BitwiseAndNodeParam.class),
    BITWISE_XOR(NodeKindCodes.BITWISE_XOR, BitwiseXorNode.class, BitwiseXorNodeParam.class),
    AND(NodeKindCodes.AND, AndNode.class, AndNodeParam.class),
    OR(NodeKindCodes.OR, OrNode.class, OrNodeParam.class),
    BITWISE_COMPLEMENT(NodeKindCodes.BITWISE_COMPLEMENT, BitwiseComplementNode.class, BitwiseComplementNodeParam.class),
    NOT(NodeKindCodes.NOT, NotNode.class, NotNodeParam.class),
    NEGATE(NodeKindCodes.NEGATE, NegateNode.class, NegateNodeParam.class),
    REM(NodeKindCodes.REM, RemainderNode.class, RemainderNodeParam.class),
    EQ(NodeKindCodes.EQ, EqNode.class, EqNodeParam.class),
    NE(NodeKindCodes.NE, NeNode.class, NeNodeParam.class),
    GE(NodeKindCodes.GE, GeNode.class, GeNodeParam.class),
    GT(NodeKindCodes.GT, GtNode.class, GtNodeParam.class),
    LT(NodeKindCodes.LT, LtNode.class, LtNodeParam.class),
    LE(NodeKindCodes.LE, LeNode.class, LeNodeParam.class),
    GET_PROPERTY(NodeKindCodes.GET_PROPERTY, GetPropertyNode.class, GetPropertyNodeParam.class),
    GET_STATIC(NodeKindCodes.GET_STATIC, GetStaticNode.class, GetStaticNodeParam.class),
    INSTANCE_OF(NodeKindCodes.INSTANCE_OF, InstanceOfNode.class, InstanceOfNodeParam.class),
    ARRAY_LENGTH(NodeKindCodes.ARRAY_LENGTH, ArrayLengthNode.class, ArrayLengthNodeParam.class),
    IF_NOT(NodeKindCodes.IF_NOT, IfNotNode.class, IfNotNodeParam.class),
    STORE(NodeKindCodes.STORE, StoreNode.class, StoreNodeParam.class),
    LOAD(NodeKindCodes.LOAD, LoadNode.class, LoadNodeParam.class),
    LOAD_CONTEXT_SLOT(NodeKindCodes.LOAD_CONTEXT_SLOT, LoadContextSlotNode.class, LoadContextSlotNodeParam.class),
    STORE_CONTEXT_SLOT(NodeKindCodes.STORE_CONTEXT_SLOT, StoreContextSlotNode.class, StoreContextSlotNodeParam.class),
    ;

    private final int code;
    private final Class<? extends NodeRT> nodeClass;
    private final Class<?> paramClass;
    private final boolean outputTypeAsChild;

    public static final Set<NodeKind> CREATING_KINDS = Set.of(ADD_OBJECT, NEW, NEW_ARRAY);

    NodeKind(int code, Class<? extends NodeRT> nodeClass, Class<?> paramClass) {
        this(code, nodeClass, paramClass, false);
    }

    NodeKind(int code, Class<? extends NodeRT> nodeClass, Class<?> paramClass, boolean outputTypeAsChild) {
        this.code = code;
        this.nodeClass = nodeClass;
        this.paramClass = paramClass;
        this.outputTypeAsChild = outputTypeAsChild;
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

    public boolean isOutputTypeAsChild() {
        return outputTypeAsChild;
    }

    public int code() {
        return code;
    }
}
