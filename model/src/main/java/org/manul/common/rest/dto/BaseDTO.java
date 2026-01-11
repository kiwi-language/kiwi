package org.manul.common.rest.dto;

import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.TmpId;

import javax.annotation.Nullable;

public interface BaseDTO {

    @Nullable String id();

    default @Nullable Long tmpId() {
        var id = id();
        if(id != null && Id.parse(id) instanceof TmpId tmpId)
            return tmpId.tmpId();
        else
            return null;
    }

}
