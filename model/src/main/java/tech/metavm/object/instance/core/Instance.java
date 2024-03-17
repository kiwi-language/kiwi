package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.NoProxy;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceParam;
import tech.metavm.object.type.Type;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Instance {

    private final Type type;

    public static final Map<Instance, Exception> STACKS = new IdentityHashMap<>();

    public Instance(@NotNull Type type) {
        this.type = type;
    }

    @NoProxy
    public Type getType() {
        return type;
    }

    @NoProxy
    public boolean isValue() {
        return type.isValue();
    }

    @NoProxy
    public boolean isNull() {
        return false;
    }

    @NoProxy
    public boolean isNotNull() {
        return !isNull();
    }

    @NoProxy
    public boolean isPassword() {
        return type.isPassword();
    }

    public Instance convert(Type type) {
        throw new BusinessException(ErrorCode.CONVERSION_FAILED, getQualifiedTitle(), type.getName());
    }

    public StringInstance toStringInstance() {
        return Instances.stringInstance(getTitle());
    }


    @NoProxy
    public boolean isArray() {
        return this instanceof ArrayInstance;
    }

    @NoProxy
    public boolean isPrimitive() {
        return false;
    }

    @NoProxy
    public boolean isNotPrimitive() {
        return !isPrimitive();
    }

    public abstract boolean isReference();

    public String toStringValue() {
        throw new UnsupportedOperationException();
    }

    public boolean isEphemeral() {
        return false;
    }

    public abstract @Nullable Id getId();

    public @Nullable String getInstanceIdString() {
        var id = getId();
        return id != null ? id.toString() : null;
    }

    public abstract FieldValue toFieldValueDTO();


    public abstract String getTitle();

    public String getQualifiedTitle() {
        return getType().getName() + "-" + getTitle();
    }


    @Override
    @NoProxy
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    @NoProxy
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public abstract void writeTo(InstanceOutput output, boolean includeChildren);

    public abstract Object toSearchConditionValue();

    public InstanceDTO toDTO() {
        return toDTO(getParam());
    }

    protected InstanceDTO toDTO(InstanceParam param) {
        try (var serContext = SerializeContext.enter()) {
            return new InstanceDTO(
                    NncUtils.get(getId(), Objects::toString),
                    serContext.getId(getType()),
                    getType().getName(),
                    getTitle(),
                    Instances.getSourceMappingId(this),
                    param
            );
        }
    }

    protected abstract InstanceParam getParam();

    @NoProxy
    public abstract <R> R accept(InstanceVisitor<R> visitor);

    public abstract <R> void acceptReferences(InstanceVisitor<R> visitor);

    public abstract <R> void acceptChildren(InstanceVisitor<R> visitor);

}
