package org.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

public class ReferenceFieldValue extends FieldValue {

    public static ReferenceFieldValue create(String id) {
        return new ReferenceFieldValue(null, id, null);
    }

    public static ReferenceFieldValue create(String id, @Nullable String typeId) {
        return new ReferenceFieldValue(null, id, typeId);
    }

    public static ReferenceFieldValue create(InstanceDTO instanceDTO) {
        return new ReferenceFieldValue(instanceDTO.title(), instanceDTO.id(), instanceDTO.type());
    }

    private final String id;
    private final String typeId;

    public ReferenceFieldValue(@JsonProperty("displayValue") String displayValue,
                               @JsonProperty("id") String id,
                               @JsonProperty("typeId") @Nullable String typeId) {
        super(FieldValueKind.REFERENCE.code(), displayValue);
        this.id = id;
        this.typeId = typeId;
    }

    public String getId() {
        return id;
    }

    public String getTypeId() {
        return typeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReferenceFieldValue that = (ReferenceFieldValue) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public String referenceId() {
        return id;
    }

    @Override
    public Object toJson() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    @Override
    public boolean valueEquals(FieldValue that, Set<String> newIds) {
        if (that instanceof ReferenceFieldValue thatReferenceFieldValue) {
            return Objects.equals(id, thatReferenceFieldValue.id);
        } else
            return false;
    }
}
