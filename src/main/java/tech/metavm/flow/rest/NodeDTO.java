package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;

public record NodeDTO(
        Long id,
        long flowId,
        String name,
        int type,
        Long prevId,
        Long outputTypeId,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
        @JsonTypeIdResolver(NodeParamTypeIdResolver.class)
        Object param,
        TypeDTO outputType,
        long scopeId
) {

    public static NodeDTO newNode(long flowId, String name, int type, Long prevId) {
        return newNode(flowId, name, type, prevId, null, 0L);
    }

    public static NodeDTO newNode(long flowId, String name, int type, Long prevId, Object param, long scopeId) {
        return new NodeDTO(
                null, flowId, name, type, prevId, null, param, null, scopeId
        );
    }

    public void ensureIdSet() {
        if(id == null) {
            throw BusinessException.invalidParams("objectId is required");
        }
    }

    public <T> T getParam() {
        return (T) param;
    }

}


