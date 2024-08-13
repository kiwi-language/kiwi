package org.metavm.entity;

public class HybridDefContextHolder implements DefContextHolder {

    private final ThreadLocal<DefContext> TL = new ThreadLocal<>();
    private DefContext defContext;

    @Override
    public DefContext get() {
        var local = TL.get();
        if(local != null)
            return local;
        return defContext;
    }

    @Override
    public void set(DefContext defContext) {
        this.defContext = defContext;
    }

    public void setLocal(DefContext defContext) {
        TL.set(defContext);
    }

    @Override
    public boolean isPresent() {
        return TL.get() != null || defContext != null;
    }

    @Override
    public void clearLocal() {
        TL.remove();
    }
}
