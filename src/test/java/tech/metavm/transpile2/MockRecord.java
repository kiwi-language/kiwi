package tech.metavm.transpile2;

public record MockRecord(
        int value
) {

    public MockRecord {
        value = Math.abs(value);
    }

    public int value(int inc) {
        return value + inc;
    }

    public MockRecord copy() {
        return new MockRecord(value());
    }

}
