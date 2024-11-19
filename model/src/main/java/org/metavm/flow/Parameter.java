package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.object.type.ITypeDef;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType
public class Parameter extends AttributedElement implements GenericElement, LocalKey, ITypeDef {

    public static Parameter create(String name, Type type) {
        return new Parameter(null, name, type);
    }

    @EntityField(asTitle = true)
    private String name;
    private Type type;
    private Callable callable;
    @Nullable
    @CopyIgnore
    private Parameter copySource;

    public Parameter(Long tmpId, String name, Type type) {
        this(tmpId, name, type, null, DummyCallable.INSTANCE);
    }

    public Parameter(Long tmpId,
                     String name,
                     Type type,
                     @Nullable Parameter copySource,
                     Callable callable) {
        setTmpId(tmpId);
        this.callable = callable;
        this.name = name;
        this.type = type;
        this.copySource = copySource;
    }

    public Callable getCallable() {
        return callable;
    }

    public void setCallable(Callable callable) {
        if (callable == this.callable)
            return;
        NncUtils.requireTrue(this.callable == DummyCallable.INSTANCE
                        || this.callable == DummyMethod.INSTANCE,
                () -> "Callable already set: " + this.callable + ", new callable: " + callable);
        this.callable = callable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Parameter copy() {
        return new Parameter(null, name, type, null, DummyCallable.INSTANCE);
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitParameter(this);
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    public String getText() {
        return name + ":" + type.getName();
    }

    public void write(KlassOutput output) {
        output.writeEntityId(this);
        output.writeUTF(name);
        type.write(output);
        writeAttributes(output);
    }

    public void read(KlassInput input) {
        name = input.readUTF();
        type = input.readType();
        readAttributes(input);
    }

}
