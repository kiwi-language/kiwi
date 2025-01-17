package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.Objects;

public interface Identifiable {

    @Nullable
    @NoProxy
    Id tryGetId();

    @NoProxy
    default Id getId() {
        return Objects.requireNonNull(tryGetId(), () -> EntityUtils.getEntityDesc(this) + " id not initialized");
    }

    @NoProxy
    default long getTreeId() {
        return getId().getTreeId();
    }

    @NoProxy
    default Long tryGetPhysicalId() {
        return Utils.safeCall(tryGetId(), Id::tryGetTreeId);
    }

}
