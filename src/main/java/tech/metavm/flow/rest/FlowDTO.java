package tech.metavm.flow.rest;

import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;

import java.util.List;

public record FlowDTO (
        Long id,
        String name,
        Long typeId,
        ScopeDTO rootScope,
        TypeDTO type,
        long inputTypeId,
        long outputTypeId
) {

    public static FlowDTO create(String name, long typeId) {
        return new FlowDTO(
                null, name, typeId, null, null, 0L, 0L
        );
    }

    public void requiredId() {
        if(id == null) {
            throw BusinessException.invalidParams("objectId is required");
        }
    }

}
