package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

public class InstanceFieldValue extends FieldValue {

    public static InstanceFieldValue of(InstanceDTO instance) {
        return new InstanceFieldValue(instance.title(), instance);
    }

    private final InstanceDTO instance;

    public InstanceFieldValue(@JsonProperty("displayValue") String displayValue,
                              @JsonProperty("instance") InstanceDTO instance) {
        super(FieldValueKind.INSTANCE_DTO.code(), displayValue);
        this.instance = instance;
    }

    public InstanceDTO getInstance() {
        return instance;
    }

    @Override
    public InstanceDTO underlyingInstance() {
        return getInstance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InstanceFieldValue that = (InstanceFieldValue) o;
        return Objects.equals(instance, that.instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instance);
    }

    @Override
    public boolean valueEquals(FieldValue that, Set<String> newIds) {
        if (that instanceof InstanceFieldValue thatInstanceFieldValue)
            return instance.valueEquals(thatInstanceFieldValue.instance, newIds);
        else
            return false;
    }
}
