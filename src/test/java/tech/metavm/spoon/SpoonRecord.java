package tech.metavm.spoon;

public record SpoonRecord(
        int value
) {

    public SpoonRecord {
        value = Math.abs(value);
    }
}
