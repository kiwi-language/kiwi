package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.metavm.common.RefDTO;

import java.util.Objects;
import java.util.Set;

public class ReferenceFieldValue extends FieldValue {

    public static ReferenceFieldValue create(String id, RefDTO typeRef) {
        return new ReferenceFieldValue(null, id, typeRef);
    }

    public static ReferenceFieldValue create(InstanceDTO instanceDTO) {
        return new ReferenceFieldValue(instanceDTO.title(), instanceDTO.id(), instanceDTO.typeRef());
    }

    private final String id;

    private final RefDTO typeRef;

    public ReferenceFieldValue(@JsonProperty("displayValue") String displayValue,
                               @JsonProperty("id") String id,
                               @JsonProperty("typeRef") RefDTO typeRef) {
        super(FieldValueKind.REFERENCE.code(), displayValue);
        this.id = id;
        this.typeRef = typeRef;
    }

    public String getId() {
        return id;
    }

    public RefDTO getTypeRef() {
        return typeRef;
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
