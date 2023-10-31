package tech.metavm.flow.rest;

import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.util.BusinessException;

import javax.annotation.Nullable;
import java.util.List;

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
        RefDTO returnTypeRef,
        List<ParameterDTO> parameters,
        RefDTO typeRef,
        RefDTO staticTypeRef,
        List<RefDTO> typeParameterRefs,
        @Nullable RefDTO templateRef,
        List<RefDTO> typeArgumentRefs,
        List<RefDTO> overriddenRefs,
        List<FlowDTO> templateInstances
) implements BaseDTO {

    public static FlowDTO create(String name, long declaringTypeId) {
        return new FlowDTO(
                null, null, name, null,false, false, false,
                RefDTO.ofId(declaringTypeId), null,  null, null,
                 null, null,  null,  null, List.of(),null, List.of()
        );
    }

    public void requiredId() {
        if (id == null) {
            throw BusinessException.invalidParams("objectId is required");
        }
    }

}
