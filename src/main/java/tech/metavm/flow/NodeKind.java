package tech.metavm.flow;

import java.util.Arrays;
import java.util.Objects;

public enum NodeKind {
    SELF(0, SelfNode.class),
    INPUT(1, InputNode.class),
//    GET_OBJECT(2, GetObjectNode.class),
    ADD_OBJECT(3, AddObjectNode.class),
    UPDATE_OBJECT(4, UpdateObjectNode.class),
    DELETE_OBJECT(5, DeleteObjectNode.class),
    GET_RELATED(6, GetRelatedNode.class),
    BRANCH(7, BranchNode.class),
    LOOP(8, LoopNode.class),
    RETURN(9, ReturnNode.class),
    EXCEPTION(10, ExceptionNode.class),
    SUB_FLOW(12, SubFlowNode.class),
    GET_UNIQUE(13, GetUniqueNode.class),

    ;

    private final int code;
    private final Class<? extends NodeRT<?>> klass;

    NodeKind(int code, Class<? extends NodeRT<?>> klass) {
        this.code = code;
        this.klass = klass;
    }

    public static NodeKind getByCodeRequired(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Flow node category " + code + " not found"));
    }


    public static NodeKind getByParamKlassRequired(Class<?> paramKlass) {
        return Arrays.stream(values())
                .filter(type -> Objects.equals(type.getParamKlass(), paramKlass))
                .findAny()
                .orElseThrow(() -> new RuntimeException("FlowNodeType not found for param class: " + paramKlass.getName()));
    }

    public Class<?> getParamKlass() {
        return NodeFactory.getParamClass(klass);
    }

    public Class<? extends NodeRT<?>> getKlass() {
        return klass;
    }

    public int code() {
        return code;
    }
}
