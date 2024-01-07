package tech.metavm.entity;

import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public interface Identifiable {

    @Nullable
    Long tryGetId();

    default long getId() {
        return NncUtils.requireNonNull(tryGetId());
    }

}
