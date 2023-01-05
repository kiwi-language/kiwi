package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ReferenceFieldValueDTO extends FieldValueDTO {

    private final long id;

    public ReferenceFieldValueDTO(@JsonProperty("displayValue") String displayValue,
                                  @JsonProperty("id") long id) {
        super(FieldValueKind.REFERENCE.code(), displayValue);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ReferenceFieldValueDTO that = (ReferenceFieldValueDTO) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
