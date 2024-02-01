package tech.metavm.entity;

public class ThreadLocalDefContextHolder implements DefContextHolder {

    private final ThreadLocal<GlobalDefContextHolder> tl = ThreadLocal.withInitial(GlobalDefContextHolder::new);

    @Override
    public DefContext get() {
        return tl.get().get();
    }

    @Override
    public void set(DefContext defContext) {
        tl.get().set(defContext);
    }
}
