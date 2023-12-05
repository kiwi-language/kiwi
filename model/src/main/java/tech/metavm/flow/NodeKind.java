package tech.metavm.flow;

import tech.metavm.flow.rest.NodeKindCodes;
import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import static tech.metavm.flow.rest.NodeKindCodes.*;

public enum NodeKind {
    SELF(NodeKindCodes.SELF, SelfNode.class),
    INPUT(NodeKindCodes.INPUT, InputNode.class, true),
    ADD_OBJECT(NodeKindCodes.ADD_OBJECT, AddObjectNode.class),
    UPDATE_OBJECT(NodeKindCodes.UPDATE_OBJECT, UpdateObjectNode.class),
    DELETE_OBJECT(NodeKindCodes.DELETE_OBJECT, DeleteObjectNode.class),
    BRANCH(NodeKindCodes.BRANCH, BranchNode.class),
    RETURN(NodeKindCodes.RETURN, ReturnNode.class),
    EXCEPTION(NodeKindCodes.EXCEPTION, RaiseNode.class),
    SUB_FLOW(NodeKindCodes.SUB_FLOW, SubFlowNode.class),
    GET_UNIQUE(NodeKindCodes.GET_UNIQUE, GetUniqueNode.class),
    MERGE(NodeKindCodes.MERGE, MergeNode.class, true),
    NEW(NodeKindCodes.NEW, NewObjectNode.class),
    VALUE(NodeKindCodes.VALUE, ValueNode.class),
    UPDATE_STATIC(NodeKindCodes.UPDATE_STATIC, UpdateStaticNode.class),
    FOREACH(NodeKindCodes.FOREACH, ForeachNode.class, true),
    WHILE(NodeKindCodes.WHILE, WhileNode.class, true),
    NEW_ARRAY(NodeKindCodes.NEW_ARRAY, NewArrayNode.class),
    CHECK(NodeKindCodes.CHECK, CheckNode.class),
    TRY(NodeKindCodes.TRY, TryNode.class),
    TRY_END(NodeKindCodes.TRY_END, TryEndNode.class, true),
    FUNC(NodeKindCodes.FUNC, FunctionNode.class),
    LAMBDA(NodeKindCodes.LAMBDA, LambdaNode.class),
    ADD_ELEMENT(NodeKindCodes.ADD_ELEMENT, AddElementNode.class),
    DELETE_ELEMENT(NodeKindCodes.DELETE_ELEMENT, DeleteElementNode.class),
    GET_ELEMENT(NodeKindCodes.GET_ELEMENT, GetElementNode.class),

    ;

    private final int code;
    private final Class<? extends NodeRT<?>> klass;
    private final boolean outputTypeAsChild;

    public static final Set<NodeKind> CREATING_KINDS = Set.of(ADD_OBJECT, NEW, NEW_ARRAY);

    NodeKind(int code, Class<? extends NodeRT<?>> klass) {
        this(code, klass, false);
    }

    NodeKind(int code, Class<? extends NodeRT<?>> klass, boolean outputTypeAsChild) {
        this.code = code;
        this.klass = klass;
        this.outputTypeAsChild = outputTypeAsChild;
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

    public static NodeKind getByNodeClass(@SuppressWarnings("rawtypes") Class<? extends NodeRT> klass) {
        return NncUtils.findRequired(values(), kind -> kind.getKlass().equals(klass));
    }

    public Class<?> getParamKlass() {
        return NodeFactory.getParamClass(klass);
    }

    public Class<? extends NodeRT<?>> getKlass() {
        return klass;
    }

    public boolean isOutputTypeAsChild() {
        return outputTypeAsChild;
    }

    public int code() {
        return code;
    }
}
