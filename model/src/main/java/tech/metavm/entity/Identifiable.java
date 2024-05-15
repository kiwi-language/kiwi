package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.util.NncUtils;

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
    default long getPhysicalId() {
        return getId().getTreeId();
    }

    @NoProxy
    default Long tryGetPhysicalId() {
        return NncUtils.get(tryGetId(), Id::tryGetTreeId);
    }

}
