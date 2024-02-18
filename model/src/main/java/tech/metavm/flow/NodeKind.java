package tech.metavm.flow;

import tech.metavm.flow.rest.*;
import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public enum NodeKind {
    SELF(NodeKindCodes.SELF, SelfNode.class, Void.class),
    INPUT(NodeKindCodes.INPUT, InputNode.class, InputNodeParam.class, true),
    ADD_OBJECT(NodeKindCodes.ADD_OBJECT, AddObjectNode.class, AddObjectNodeParam.class),
    UPDATE_OBJECT(NodeKindCodes.UPDATE_OBJECT, UpdateObjectNode.class, UpdateObjectNodeParam.class),
    DELETE_OBJECT(NodeKindCodes.DELETE_OBJECT, DeleteObjectNode.class, DeleteObjectNodeParam.class),
    BRANCH(NodeKindCodes.BRANCH, BranchNode.class, BranchNodeParam.class),
    RETURN(NodeKindCodes.RETURN, ReturnNode.class, ReturnNodeParam.class),
    EXCEPTION(NodeKindCodes.EXCEPTION, RaiseNode.class, RaiseNodeParam.class),
    METHOD_CALL(NodeKindCodes.METHOD_CALL, MethodCallNode.class, MethodCallNodeParam.class),
    GET_UNIQUE(NodeKindCodes.GET_UNIQUE, GetUniqueNode.class, GetUniqueNodeParam.class),
    MERGE(NodeKindCodes.MERGE, MergeNode.class, MergeNodeParam.class, true),
    NEW(NodeKindCodes.NEW, NewObjectNode.class, NewObjectNodeParam.class),
    VALUE(NodeKindCodes.VALUE, ValueNode.class, ValueNodeParam.class),
    UPDATE_STATIC(NodeKindCodes.UPDATE_STATIC, UpdateStaticNode.class, UpdateStaticNodeParam.class),
    FOREACH(NodeKindCodes.FOREACH, ForeachNode.class, ForeachNodeParam.class, true),
    WHILE(NodeKindCodes.WHILE, WhileNode.class, WhileNodeParam.class, true),
    NEW_ARRAY(NodeKindCodes.NEW_ARRAY, NewArrayNode.class, NewArrayNodeParam.class),
    CHECK(NodeKindCodes.CHECK, CheckNode.class, CheckNodeParam.class),
    TRY(NodeKindCodes.TRY, TryNode.class, TryNodeParam.class),
    TRY_END(NodeKindCodes.TRY_END, TryEndNode.class, TryEndNodeParam.class, true),
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

    public static NodeKind getByCodeRequired(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Node kind " + code + " not found"));
    }


    public static NodeKind getByParamClassRequired(Class<?> paramClass) {
        return Arrays.stream(values())
                .filter(kind -> Objects.equals(kind.getParamKlass(), paramClass))
                .findAny()
                .orElseThrow(() -> new RuntimeException("NodeKind not found for param class: " + paramClass.getName()));
    }

    public static NodeKind getByNodeClass(Class<? extends NodeRT> klass) {
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
