package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public interface Identifiable {

    @Nullable
    @NoProxy
    Id tryGetId();

    @NoProxy
    default Id getId() {
        return NncUtils.requireNonNull(tryGetId());
    }

    @NoProxy
    default long getPhysicalId() {
        return getId().getPhysicalId();
    }

    @NoProxy
    default Long tryGetPhysicalId() {
        return NncUtils.get(tryGetId(), Id::tryGetPhysicalId);
    }

}
