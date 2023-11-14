package tech.metavm.flow;

import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public enum NodeKind {
    SELF(0, SelfNode.class),
    INPUT(1, InputNode.class, true),
    ADD_OBJECT(3, AddObjectNode.class),
    UPDATE_OBJECT(4, UpdateObjectNode.class),
    DELETE_OBJECT(5, DeleteObjectNode.class),
    BRANCH(7, BranchNode.class),
    RETURN(9, ReturnNode.class),
    EXCEPTION(10, RaiseNode.class),
    SUB_FLOW(12, SubFlowNode.class),
    GET_UNIQUE(13, GetUniqueNode.class),
    MERGE(14, MergeNode.class, true),
    NEW(15, NewObjectNode.class),
    VALUE(16, ValueNode.class),
    UPDATE_STATIC(17, UpdateStaticNode.class),
    FOREACH(19, ForeachNode.class, true),
    WHILE(20, WhileNode.class, true),
    NEW_ARRAY(21, NewArrayNode.class),
    CHECK(22, CheckNode.class),
    TRY(23, TryNode.class),
    TRY_END(24, TryEndNode.class, true),
    FUNC(25, FunctionNode.class),
    LAMBDA(26, LambdaNode.class),
    ADD_ELEMENT(27, AddElementNode.class),
    DELETE_ELEMENT(28, DeleteElementNode.class),
    GET_ELEMENT(29, GetElementNode.class),

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
