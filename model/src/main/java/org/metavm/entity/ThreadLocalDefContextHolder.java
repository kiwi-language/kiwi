package org.metavm.entity;

public class ThreadLocalDefContextHolder implements DefContextHolder {

    private final ThreadLocal<GlobalDefContextHolder> tl = ThreadLocal.withInitial(GlobalDefContextHolder::new);

    @Override
    public SystemDefContext get() {
        return tl.get().get();
    }

    @Override
    public void set(SystemDefContext defContext) {
        tl.get().set(defContext);
    }

    @Override
    public boolean isPresent() {
        return tl.get().isPresent();
    }
}
