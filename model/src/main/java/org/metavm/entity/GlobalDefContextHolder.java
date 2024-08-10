package org.metavm.entity;

import java.util.Objects;

public class GlobalDefContextHolder implements DefContextHolder {

    private DefContext defContext;

    @Override
    public DefContext get() {
        return Objects.requireNonNull(defContext);
    }

    @Override
    public void set(DefContext defContext) {
        this.defContext = defContext;
    }

    @Override
    public boolean isPresent() {
        return defContext != null;
    }
}
