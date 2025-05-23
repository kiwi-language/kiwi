package org.metavm.object.instance.rest.dto;

public record ShortValueDTO(short value) implements IntegerValueDTO {
    @Override
    public String getKind() {
        return "short";
    }

    @Override
    public byte byteValue() {
        return (byte) value;
    }

    @Override
    public short shortValue() {
        return value;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }
}
