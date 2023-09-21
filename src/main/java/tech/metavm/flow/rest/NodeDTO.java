package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.flow.NodeKind;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;

public record NodeDTO(
        Long tmpId,
        Long id,
        Long flowId,
        String name,
        int kind,
        RefDTO prevRef,
        RefDTO outputTypeRef,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
        @JsonTypeIdResolver(NodeParamTypeIdResolver.class)
        Object param,
        TypeDTO outputType,
        Long scopeId
) implements BaseDTO {

    public static NodeDTO create(String name, NodeKind kind) {
        return new NodeDTO(null, null, null, name, kind.code(), null, null, null, null, null);
    }

    public static NodeDTO newNode(long flowId, String name, int type, Long prevId) {
        return newNode(flowId, name, type, prevId, null, 0L);
    }

    public static NodeDTO newNode(long flowId, String name, int type, Long prevId, Object param, long scopeId) {
        return new NodeDTO(
                null, null, flowId, name, type, RefDTO.ofId(prevId), null, param, null, scopeId
        );
    }

    public NodeDTO copyWithParam(Object newParam) {
        return new NodeDTO(
                null,
                id,
                flowId,
                name,
                kind,
                prevRef,
                outputTypeRef,
                newParam,
                outputType,
                scopeId
        );
    }

    public NodeDTO copyWithType(TypeDTO type) {
        return new NodeDTO(
                null,
                id,
                flowId,
                name,
                kind,
                prevRef,
                new RefDTO(type.id(), type.tmpId()),
                param,
                type,
                scopeId
        );
    }

    public NodeDTO copyWithParamAndType(Object param, TypeDTO type) {
        return new NodeDTO(
                null,
                id,
                flowId,
                name,
                kind,
                prevRef,
                new RefDTO(type.id(), type.tmpId()),
                param,
                type,
                scopeId
        );
    }

    public void ensureIdSet() {
        if(id == null) {
            throw BusinessException.invalidParams("objectId is required");
        }
    }

    public <T> T getParam() {
        //noinspection unchecked
        return (T) param;
    }

}


