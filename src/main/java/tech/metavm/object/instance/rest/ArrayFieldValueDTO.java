package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class ArrayFieldValueDTO extends FieldValue {

    private final Long id;
    private final boolean elementAsChild;
    private final List<FieldValue> elements;

    public ArrayFieldValueDTO(@JsonProperty("id") Long id,
                              @JsonProperty("elementAsChild") boolean elementAsChild,
                              @JsonProperty("elements") List<FieldValue> elements) {
        super(FieldValueKind.ARRAY.code(), "");
        this.id = id;
        this.elementAsChild = elementAsChild;
        this.elements = elements;
    }

    public List<FieldValue> getElements() {
        return elements;
    }

    public boolean isElementAsChild() {
        return elementAsChild;
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
