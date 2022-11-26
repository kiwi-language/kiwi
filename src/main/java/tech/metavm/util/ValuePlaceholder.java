package tech.metavm.util;

public class ValuePlaceholder<T> {

    private T value;

    public T get() {
        if(value == null) {
            throw new IllegalStateException("Value is not set yet");
        }
        return value;
    }

    public void set(T value) {
        if(this.value != null) {
            throw new IllegalStateException("Value is already set");
        }
        this.value = value;
    }

}
