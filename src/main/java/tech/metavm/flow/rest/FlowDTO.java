package tech.metavm.flow.rest;

import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.BusinessException;

import javax.annotation.Nullable;

public record FlowDTO(
        Long tmpId,
        Long id,
        String name,
        String code,
        boolean isConstructor,
        boolean isAbstract,
        boolean isNative,
        RefDTO declaringTypeRef,
        ScopeDTO rootScope,
        TypeDTO type,
        RefDTO inputTypeRef,
        RefDTO outputTypeRef,
        TypeDTO inputType,
        TypeDTO outputType,
        @Nullable RefDTO overridenRef
) implements BaseDTO {

    public Long inputTypeId() {
        return inputTypeRef.id();
    }

    public Long outputTypeId() {
        return outputTypeRef.id();
    }

    public static FlowDTO create(String name, long declaringTypeId) {
        return new FlowDTO(
                null, null, name, null, false, false, false,
                RefDTO.ofId(declaringTypeId), null, null, null, null,
                null, null, null
        );
    }

    public void requiredId() {
        if (id == null) {
            throw BusinessException.invalidParams("objectId is required");
        }
    }

}
