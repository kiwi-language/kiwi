package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PrimitiveFieldValueDTO extends FieldValueDTO {

    private final Object value;

    public PrimitiveFieldValueDTO(
            @JsonProperty("displayValue") String displayValue,
            @JsonProperty("value") Object value) {
        super(FieldValueKind.PRIMITIVE.code(), displayValue);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrimitiveFieldValueDTO that = (PrimitiveFieldValueDTO) o;
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
