package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class ArrayFieldValueDTO extends FieldValueDTO {

    private final Long id;
    private final List<FieldValueDTO> elements;

    public ArrayFieldValueDTO(@JsonProperty("id") Long id,
                              @JsonProperty("elements") List<FieldValueDTO> elements) {
        super(FieldValueKind.ARRAY.code(), "");
        this.id = id;
        this.elements = elements;
    }

    public List<FieldValueDTO> getElements() {
        return elements;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ArrayFieldValueDTO that = (ArrayFieldValueDTO) o;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elements);
    }
}
