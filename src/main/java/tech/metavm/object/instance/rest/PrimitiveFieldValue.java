package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PrimitiveFieldValue extends FieldValue {

    private final Object value;
    private final int primitiveKind;

    public PrimitiveFieldValue(
            @JsonProperty("displayValue") String displayValue,
            @JsonProperty("primitiveKind") int primitiveKind,
            @JsonProperty("value") Object value) {
        super(FieldValueKind.PRIMITIVE.code(), displayValue);
        this.primitiveKind = primitiveKind;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public int getPrimitiveKind() {
        return primitiveKind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrimitiveFieldValue that = (PrimitiveFieldValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        return "PrimitiveFieldValueDTO{" +
                "value=" + value +
                '}';
    }
}
