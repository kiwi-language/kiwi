package tech.metavm.entity;

import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public interface Identifiable {

    @Nullable
    Long getId();

    default long getIdRequired() {
        return NncUtils.requireNonNull(getId());
    }

}
