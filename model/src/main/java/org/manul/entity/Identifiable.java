package org.manul.entity;

import org.manul.object.instance.core.Id;
import org.manul.util.Utils;

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
