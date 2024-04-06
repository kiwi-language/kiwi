package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType("参数")
public class Parameter extends Element implements GenericElement, LocalKey {
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
    @Nullable
    @CopyIgnore
    private Parameter copySource;

    public Parameter(Long tmpId, String name, @Nullable String code, Type type) {
        this(tmpId, name, code, type, null, null, DummyCallable.INSTANCE);
    }

    public Parameter(Long tmpId, String name, @Nullable String code, Type type,
                     @Nullable Value condition,
                     @Nullable Parameter copySource,
                     Callable callable) {
        setTmpId(tmpId);
        this.callable = callable;
        this.name = name;
        this.code = code;
        this.type = type;
        this.copySource = copySource;
        setCondition(condition);
    }

    public Callable getCallable() {
        return callable;
    }

    public void setCallable(Callable callable) {
        if (callable == this.callable)
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
        try (var serContext = SerializeContext.enter()) {
            return new ParameterDTO(
                    serContext.getId(this),
                    name,
                    code,
                    serContext.getId(type),
                    NncUtils.get(condition, Value::toDTO),
                    NncUtils.get(copySource, serContext::getId),
                    serContext.getId(callable)
            );
        }
    }

    @Nullable
    public Parameter getCopySource() {
        return copySource;
    }

    @Override
    public void setCopySource(Object copySource) {
        NncUtils.requireNull(this.copySource);
        this.copySource = (Parameter) copySource;
    }

    public @Nullable Value getCondition() {
        return condition;
    }

    public void setCondition(@Nullable Value condition) {
        this.condition = NncUtils.get(condition, c -> addChild(c, "condition"));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParameter(this);
    }

    @Override
    public boolean isValidLocalKey() {
        return code != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(code);
    }

    public String getText() {
        return name + ":" + type.getName();
    }
}
