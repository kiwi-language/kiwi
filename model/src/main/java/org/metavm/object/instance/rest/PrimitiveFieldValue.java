package org.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metavm.object.type.PrimitiveKind;

import java.util.Objects;
import java.util.Set;

public class PrimitiveFieldValue extends FieldValue {

    public static final PrimitiveFieldValue NULL = new PrimitiveFieldValue(
            "null", PrimitiveKind.NULL.code(), null
    );

    public static final PrimitiveFieldValue TRUE = new PrimitiveFieldValue(
            "true", PrimitiveKind.BOOLEAN.code(), true
    );

    public static final PrimitiveFieldValue FALSE = new PrimitiveFieldValue(
            "false", PrimitiveKind.BOOLEAN.code(), false
    );

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

    public static PrimitiveFieldValue createString(String value) {
        return new PrimitiveFieldValue(value, PrimitiveKind.STRING.code(), value);
    }

    public static PrimitiveFieldValue createLong(long value) {
        return new PrimitiveFieldValue(Long.toString(value), PrimitiveKind.LONG.code(), value);
    }

    public static PrimitiveFieldValue createDouble(double value) {
        return new PrimitiveFieldValue(Double.toString(value), PrimitiveKind.DOUBLE.code(), value);
    }

    public static PrimitiveFieldValue createTime(long value) {
        return new PrimitiveFieldValue(null, PrimitiveKind.TIME.code(), value);
    }

    public static PrimitiveFieldValue createPassword(String value) {
        return new PrimitiveFieldValue(null, PrimitiveKind.PASSWORD.code(), value);
    }

    public static PrimitiveFieldValue createBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static PrimitiveFieldValue createNull() {
        return new PrimitiveFieldValue("null", PrimitiveKind.NULL.code(), null);
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
    public boolean valueEquals(FieldValue that, Set<String> newIds) {
        if (that instanceof PrimitiveFieldValue thatPrimitiveFieldValue) {
            return primitiveKind == thatPrimitiveFieldValue.primitiveKind
                    && Objects.equals(value, thatPrimitiveFieldValue.value);
        } else
            return false;
    }

    @Override
    public Object toJson() {
        return value;
    }

    @Override
    public String toString() {
        return "PrimitiveFieldValueDTO{" +
                "value=" + value +
                '}';
    }


}
