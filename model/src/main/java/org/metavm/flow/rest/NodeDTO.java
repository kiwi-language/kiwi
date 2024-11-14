package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;

public record NodeDTO(
        @Nullable String id,
        String flowId,
        String name,
        int kind,
        String prevId,
        String outputType,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
        Object param,
        KlassDTO outputKlass,
        String scopeId,
        @Nullable String error
) implements BaseDTO {

    public static NodeDTO create(String name, int kind) {
        return new NodeDTO(null, null, name,  kind, null, null, null, null, null, null);
    }

    public void ensureIdSet() {
        if (id == null) {
            throw new InternalException("objectId is required");
        }
    }

    public <T> T getParam() {
        //noinspection unchecked
        return (T) param;
    }

}


