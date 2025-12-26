package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.Objects;

public interface Identifiable {

    @Nullable
    Id tryGetId();

    default Id getId() {
        return Objects.requireNonNull(tryGetId(), () -> EntityUtils.getEntityDesc(this) + " id not initialized");
    }

    default long getTreeId() {
        return getId().getTreeId();
    }

    default Long tryGetPhysicalId() {
        return Utils.safeCall(tryGetId(), Id::tryGetTreeId);
    }

}
