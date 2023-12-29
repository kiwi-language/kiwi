package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public record FlowDTO(
        Long id,
        Long tmpId,
        String name,
        String code,
        boolean isNative,
        @Nullable ScopeDTO rootScope,
        RefDTO returnTypeRef,
        List<ParameterDTO> parameters,
        RefDTO typeRef,
        List<RefDTO> typeParameterRefs,
        @Nullable RefDTO horizontalTemplateRef,
        List<RefDTO> typeArgumentRefs,
        boolean isTemplate,
        int state,
        FlowParam param
) implements BaseDTO, GenericDeclarationDTO {

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

    @JsonIgnore
    public boolean isMethod() {
        return param instanceof MethodParam;
    }

    @JsonIgnore
    public boolean isConstructor() {
        return param instanceof MethodParam methodParam && methodParam.isConstructor();
    }

}
