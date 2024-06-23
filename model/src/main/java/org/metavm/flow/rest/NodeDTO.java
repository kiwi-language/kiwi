package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.util.Constants;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

public record NodeDTO(
        @Nullable String id,
        String flowId,
        String name,
        @Nullable String code,
        int kind,
        String prevId,
        String outputType,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
        @JsonTypeIdResolver(NodeParamTypeIdResolver.class)
        Object param,
        KlassDTO outputKlass,
        String scopeId,
        @Nullable String error
) implements BaseDTO, Copyable<NodeDTO> {

    public static NodeDTO create(String name, int kind) {
        return new NodeDTO(null, null, name, null, kind, null, null, null, null, null, null);
    }

    public static NodeDTO newNode(String flowId, String name, int type, String prevId) {
        return newNode(flowId, name, type, prevId, null, null);
    }

    public static NodeDTO newNode(String flowId, String name, int type, String prevId, Object param, String scopeId) {
        return new NodeDTO(
                null, flowId, name, null, type, prevId,
                null, param, null, scopeId, null
        );
    }

    public NodeDTO copyWithParam(Object newParam) {
        return new NodeDTO(
                id,
                flowId,
                name,
                code,
                kind,
                prevId,
                outputType,
                newParam,
                outputKlass,
                scopeId,
                error
        );
    }

    public NodeDTO copyWithType(KlassDTO type) {
        return new NodeDTO(
                id,
                flowId,
                name,
                code,
                kind,
                prevId,
                NncUtils.get(type.id(), id -> Constants.ID_PREFIX + id),
                param,
                type,
                scopeId,
                error
        );
    }

    public NodeDTO copyWithParamAndType(Object param, KlassDTO type) {
        return new NodeDTO(
                id,
                flowId,
                name,
                code,
                kind,
                prevId,
                NncUtils.get(type.id(), id -> Constants.ID_PREFIX + id),
                param,
                type,
                scopeId,
                error
        );
    }

    public NodeDTO copyWithPrevId(String prevId) {
        return new NodeDTO(
                id,
                flowId,
                name,
                code,
                kind,
                prevId,
                outputType,
                param,
                outputKlass,
                scopeId,
                error
        );
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

    @Override
    public NodeDTO copy(CopyContext context) {
        return new NodeDTO(
                context.mapId(id),
                context.mapId(flowId),
                name,
                code,
                kind,
                context.mapId(prevId),
                outputType,
                context.copy(param),
                context.copy(outputKlass),
                context.mapId(scopeId),
                error
        );
    }
}


