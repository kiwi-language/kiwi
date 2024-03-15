package tech.metavm.common;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;

import javax.annotation.Nullable;

public interface BaseDTO {

    @Nullable String id();

    default @Nullable Long tmpId() {
        var id = id();
        if(id != null && Id.parse(id) instanceof TmpId tmpId)
            return tmpId.getTmpId();
        else
            return null;
    }

}
