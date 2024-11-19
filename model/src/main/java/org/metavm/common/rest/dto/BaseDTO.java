package org.metavm.common.rest.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TmpId;
import org.metavm.util.IdDeserializer;

import javax.annotation.Nullable;

public interface BaseDTO {

    @JsonDeserialize(using = IdDeserializer.class)
    @Nullable String id();

    default @Nullable Long tmpId() {
        var id = id();
        if(id != null && Id.parse(id) instanceof TmpId tmpId)
            return tmpId.tmpId();
        else
            return null;
    }

}
