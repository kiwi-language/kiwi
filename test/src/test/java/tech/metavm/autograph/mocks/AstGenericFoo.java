package tech.metavm.autograph.mocks;

public class AstGenericFoo<T> {

    private T value;

    public AstGenericFoo(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public <E extends AstGenericFoo<T>> void copyValue(E that) {
        this.value = that.getValue();
    }

}
