package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.rest.ParameterDTO;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class Parameter extends AttributedElement implements GenericElement, LocalKey {
    @EntityField(asTitle = true)
    private String name;
    @EntityField(asKey = true)
    private @Nullable String code;
    private Type type;
    @Nullable
    private Value condition;
    private Callable callable;
    @Nullable
    @CopyIgnore
    private Parameter copySource;

    public Parameter(Long tmpId, String name, @Nullable String code, Type type) {
        this(tmpId, name, code, type, null, null, DummyCallable.INSTANCE);
    }

    public Parameter(Long tmpId,
                     String name,
                     @Nullable String code,
                     Type type,
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
                    serContext.getStringId(this),
                    name,
                    code,
                    type.toExpression(serContext),
                    NncUtils.get(condition, Value::toDTO),
                    NncUtils.get(copySource, serContext::getStringId),
                    getAttributesMap(),
                    serContext.getStringId(callable)
            );
        }
    }

    @Nullable
    public Parameter getCopySource() {
        return copySource;
    }

    public Parameter getUltimateTemplate() {
        return getEffectiveVerticalTemplate().getEffectiveHorizontalTemplate().getEffectiveVerticalTemplate();
    }

    public Parameter getEffectiveHorizontalTemplate() {
        if(callable instanceof Flow flow && flow.isParameterized())
            return Objects.requireNonNull(copySource);
        else
            return this;
    }

    public Parameter getEffectiveVerticalTemplate() {
        if(callable instanceof Method method && !method.isParameterized() && method.getDeclaringType().isParameterized())
            return Objects.requireNonNull(copySource);
        else
            return this;
    }

    public ParameterRef getRef() {
        return new ParameterRef(callable.getRef(), getUltimateTemplate());
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
        this.condition = condition;
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
