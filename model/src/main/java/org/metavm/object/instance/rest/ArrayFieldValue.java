package org.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ArrayFieldValue extends FieldValue {

    private final String id;
    private final boolean elementAsChild;
    private final List<FieldValue> elements;

    public ArrayFieldValue(@JsonProperty("id") String id,
                           @JsonProperty("elementAsChild") boolean elementAsChild,
                           @JsonProperty("elements") List<FieldValue> elements) {
        super("");
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
    public int getKind() {
        return FieldValueKind.ARRAY.code();
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

    @Override
    public boolean valueEquals(FieldValue that, Set<String> newIds) {
        if (that instanceof ArrayFieldValue thatArrayFieldValue) {
            return Objects.equals(id, thatArrayFieldValue.id)
                    && elementAsChild == thatArrayFieldValue.elementAsChild
                    && NncUtils.listEquals(elements, thatArrayFieldValue.elements,
                    (fieldValue, that1) -> fieldValue.valueEquals(that1, newIds));
        } else
            return false;
    }

    @Override
    public Object toJson() {
        return NncUtils.map(elements, FieldValue::toJson);
    }

}
