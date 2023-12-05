package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class InstanceFieldValue extends FieldValue {

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
}
