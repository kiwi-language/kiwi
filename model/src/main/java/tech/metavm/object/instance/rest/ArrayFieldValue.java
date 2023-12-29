package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class ArrayFieldValue extends FieldValue {

    private final String id;
    private final boolean elementAsChild;
    private final List<FieldValue> elements;

    public ArrayFieldValue(@JsonProperty("id") String id,
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

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ArrayFieldValue that = (ArrayFieldValue) o;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elements);
    }
}
