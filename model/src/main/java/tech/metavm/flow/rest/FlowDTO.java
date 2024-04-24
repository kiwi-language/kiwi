package tech.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.common.BaseDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public record FlowDTO(
        String id,
        String name,
        String code,
        boolean isNative,
        @Nullable ScopeDTO rootScope,
        String returnType,
        List<ParameterDTO> parameters,
        String typeId,
        List<String> typeParameterIds,
        @Nullable String horizontalTemplateId,
        List<String> typeArguments,
        List<String> capturedTypeIds,
        List<String> capturedCompositeTypeIds,
        List<String> capturedFlowIds,
        boolean isTemplate,
        int state,
        FlowParam param
) implements BaseDTO, GenericDeclarationDTO {

    @JsonIgnore
    public FlowSignatureDTO signature() {
        return new FlowSignatureDTO(
                name, NncUtils.map(parameters, ParameterDTO::type)
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
