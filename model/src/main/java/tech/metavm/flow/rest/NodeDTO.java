package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public record NodeDTO(
        Long id,
        Long tmpId,
        Long flowId,
        String name,
        @Nullable String code,
        int kind,
        RefDTO prevRef,
        RefDTO outputTypeRef,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
        @JsonTypeIdResolver(NodeParamTypeIdResolver.class)
        Object param,
        TypeDTO outputType,
        Long scopeId,
        @Nullable  String error
) implements BaseDTO {

    public static NodeDTO create(String name, int kind) {
        return new NodeDTO(null, null, null, name, null, kind, null, null, null, null, null, null);
    }

    public static NodeDTO newNode(long flowId, String name, int type, Long prevId) {
        return newNode(flowId, name, type, prevId, null, 0L);
    }

    public static NodeDTO newNode(long flowId, String name, int type, Long prevId, Object param, long scopeId) {
        return new NodeDTO(
                null, null, flowId, name, null, type, NncUtils.get(prevId, RefDTO::fromId),
                null, param, null, scopeId, null
        );
    }

    public NodeDTO copyWithParam(Object newParam) {
        return new NodeDTO(
                id,
                tmpId,
                flowId,
                name,
                code,
                kind,
                prevRef,
                outputTypeRef,
                newParam,
                outputType,
                scopeId,
                error
        );
    }

    public NodeDTO copyWithType(TypeDTO type) {
        return new NodeDTO(
                id,
                tmpId,
                flowId,
                name,
                code,
                kind,
                prevRef,
                new RefDTO(type.id(), type.tmpId()),
                param,
                type,
                scopeId,
                error
        );
    }

    public NodeDTO copyWithParamAndType(Object param, TypeDTO type) {
        return new NodeDTO(
                id,
                tmpId,
                flowId,
                name,
                code,
                kind,
                prevRef,
                new RefDTO(type.id(), type.tmpId()),
                param,
                type,
                scopeId,
                error
        );
    }

    public NodeDTO copyWithPrevRef(RefDTO prevRef) {
        return new NodeDTO(
                id,
                tmpId,
                flowId,
                name,
                code,
                kind,
                prevRef,
                outputTypeRef,
                param,
                outputType,
                scopeId,
                error
        );
    }

    public void ensureIdSet() {
        if(id == null) {
            throw new InternalException("objectId is required");
        }
    }

    public <T> T getParam() {
        //noinspection unchecked
        return (T) param;
    }

}


