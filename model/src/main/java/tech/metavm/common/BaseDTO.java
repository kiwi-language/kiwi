package tech.metavm.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.util.IdDeserializer;

import javax.annotation.Nullable;

public interface BaseDTO {

    @JsonDeserialize(using = IdDeserializer.class)
    @Nullable String id();

    default @Nullable Long tmpId() {
        var id = id();
        if(id != null && Id.parse(id) instanceof TmpId tmpId)
            return tmpId.getTmpId();
        else
            return null;
    }

}
