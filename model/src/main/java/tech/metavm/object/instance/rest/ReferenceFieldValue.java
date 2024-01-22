package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Set;

public class ReferenceFieldValue extends FieldValue {

    public static ReferenceFieldValue create(String id) {
        return new ReferenceFieldValue(null, id);
    }

    private final String id;

    public ReferenceFieldValue(@JsonProperty("displayValue") String displayValue,
                               @JsonProperty("id") String id) {
        super(FieldValueKind.REFERENCE.code(), displayValue);
        this.id = id;
    }

    public String getId() {
        return id;
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
