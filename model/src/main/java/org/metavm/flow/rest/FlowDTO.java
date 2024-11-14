package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.object.type.rest.dto.ConstantPoolDTO;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public record FlowDTO(
        String id,
        String name,
        String code,
        boolean isNative,
        boolean synthetic,
        @Nullable ScopeDTO rootScope,
        String returnType,
        List<ParameterDTO> parameters,
        String type,
        List<String> typeParameterIds,
        @Nullable String horizontalTemplateId,
        List<String> typeArguments,
        List<String> capturedTypeIds,
        List<String> capturedCompositeTypeIds,
        List<String> capturedFlowIds,
        List<LambdaDTO> lambdas,
        boolean isTemplate,
        Map<String, String> attributes,
        int state,
        ConstantPoolDTO constantPool,
        FlowParam param
) implements BaseDTO, GenericDeclarationDTO {

    @JsonIgnore
    public FlowSignatureDTO signature() {
        return new FlowSignatureDTO(
                name, NncUtils.map(parameters, ParameterDTO::type)
        );
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
