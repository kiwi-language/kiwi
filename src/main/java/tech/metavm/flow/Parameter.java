package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("参数")
public class Parameter extends Element {
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("编号")
    @Nullable
    private String code;
    @EntityField("类型")
    private Type type;
    @ChildEntity("条件")
    @Nullable
    private Value condition;

    private final @Nullable Parameter template;

    public Parameter(Long tmpId, String name, @Nullable String code, Type type) {
        this(tmpId, name, code, type, null, null);
    }

    public Parameter(Long tmpId, String name, @Nullable String code, Type type,
                     @Nullable Value condition,
                     @Nullable Parameter template) {
        setTmpId(tmpId);
        this.name = name;
        this.code = code;
        this.type = type;
        this.template = template;
        this.condition = condition;
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
        return new Parameter(null, name, code, type, condition, null);
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
                    NncUtils.get(template, context::getRef)
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
