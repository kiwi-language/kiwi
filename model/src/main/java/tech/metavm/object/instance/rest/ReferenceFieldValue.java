package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ReferenceFieldValue extends FieldValue {

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
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
