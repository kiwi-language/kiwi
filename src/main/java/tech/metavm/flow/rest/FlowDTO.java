package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

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
        @Nullable ScopeDTO rootScope,
        RefDTO returnTypeRef,
        List<ParameterDTO> parameters,
        RefDTO typeRef,
        RefDTO staticTypeRef,
        List<RefDTO> typeParameterRefs,
        @Nullable RefDTO templateRef,
        List<RefDTO> typeArgumentRefs,
        List<RefDTO> overriddenRefs,
        List<FlowDTO> templateInstances,
        boolean error
) implements BaseDTO, GenericDeclarationDTO {

    public static FlowDTO create(String name, long declaringTypeId) {
        return new FlowDTO(
                null, null, name, null,false, false, false,
                RefDTO.fromId(declaringTypeId), null,  null, null,
                 null, null,  null,  null, List.of(),null, List.of(), false
        );
    }

    @JsonIgnore
    public FlowSignatureDTO signature() {
        return new FlowSignatureDTO(
                name, NncUtils.map(parameters, ParameterDTO::typeRef)
        );
    }

    public void requiredId() {
        if (id == null) {
            throw BusinessException.invalidParams("objectId is required");
        }
    }

    @JsonIgnore
    public ParameterDTO findParameterByName(String name) {
        return NncUtils.find(parameters, param -> param.name().equals(name));
    }

}
