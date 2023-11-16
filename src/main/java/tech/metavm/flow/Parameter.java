package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("参数")
public class Parameter extends Element {
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField(value = "编号", asKey = true)
    @Nullable
    private String code;
    @EntityField("类型")
    private Type type;
    @ChildEntity("条件")
    @Nullable
    private Value condition;
    @EntityField("可调用")
    private Callable callable;
    @EntityField("模板")
    private final @Nullable Parameter template;

    public Parameter(Long tmpId, String name, @Nullable String code, Type type) {
        this(tmpId, name, code, type, null, null, DummyCallable.INSTANCE);
    }

    public Parameter(Long tmpId, String name, @Nullable String code, Type type,
                     @Nullable Value condition,
                     @Nullable Parameter template,
                     Callable callable) {
        setTmpId(tmpId);
        this.callable = callable;
        this.name = name;
        this.code = code;
        this.type = type;
        this.template = template;
        this.condition = condition;
    }

    public Callable getCallable() {
        return callable;
    }

    public void setCallable(Callable callable) {
        if(callable == this.callable)
            return;
        NncUtils.requireTrue(this.callable == DummyCallable.INSTANCE,
                "Callable already set");
        this.callable = callable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public Type getType() {
        return type;
    }

    public Parameter copy() {
        return new Parameter(null, name, code, type, condition, null, DummyCallable.INSTANCE);
    }

    public ParameterDTO toDTO() {
        try (var context = SerializeContext.enter()) {
            return new ParameterDTO(
                    context.getTmpId(this),
                    getId(),
                    name,
                    code,
                    context.getRef(type),
                    NncUtils.get(condition, v -> v.toDTO(false)),
                    NncUtils.get(template, context::getRef),
                    context.getRef(callable)
            );
        }
    }

    @Nullable
    public Parameter getTemplate() {
        return template;
    }

    public @Nullable Value getCondition() {
        return condition;
    }

    public void setCondition(@Nullable Value condition) {
        this.condition = condition;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParameter(this);
    }
}
