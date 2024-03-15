package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public interface Identifiable {

    @Nullable
    Id tryGetId();

    default Id getId() {
        return NncUtils.requireNonNull(tryGetId());
    }

    default long getPhysicalId() {
        return getId().getPhysicalId();
    }

}
