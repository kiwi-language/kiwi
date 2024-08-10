package org.metavm.entity;

import java.util.Objects;

public class GlobalDefContextHolder implements DefContextHolder {

    private SystemDefContext defContext;

    @Override
    public SystemDefContext get() {
        return Objects.requireNonNull(defContext);
    }

    @Override
    public void set(SystemDefContext defContext) {
        this.defContext = defContext;
    }

    @Override
    public boolean isPresent() {
        return defContext != null;
    }
}
