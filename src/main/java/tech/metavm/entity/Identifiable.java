package tech.metavm.entity;

import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public interface Identifiable {

    @Nullable
    Long getId();

    default Long getIdRequired() {
        return NncUtils.requireNonNull(getId());
    }

}
