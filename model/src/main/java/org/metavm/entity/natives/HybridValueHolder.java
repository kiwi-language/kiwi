package org.metavm.entity.natives;

public class HybridValueHolder<T> implements ValueHolder<T> {

    private final ThreadLocal<T> TL = new ThreadLocal<>();
    private T value;

    @Override
    public T get() {
        var local = TL.get();
        if(local != null)
            return local;
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    @Override
    public void setLocal(T value) {
        TL.set(value);
    }

    @Override
    public void clearLocal() {
        TL.remove();
    }

    public boolean isLocalPresent() {
        return TL.get() != null;
    }

}
