package tech.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.List;

public class LambdaNodeParam extends ScopeNodeParamDTO {

    private final List<ParameterDTO> parameters;
    private final String returnTypeId;
    @Nullable
    private final String functionalInterfaceId;

    public LambdaNodeParam(ScopeDTO bodyScope, List<ParameterDTO> parameters, String returnTypeId,
                           @Nullable String functionalInterfaceId) {
        super(bodyScope);
        this.parameters = parameters;
        this.returnTypeId = returnTypeId;
        this.functionalInterfaceId = functionalInterfaceId;
    }

    public List<ParameterDTO> getParameters() {
        return parameters;
    }

    public String getReturnTypeId() {
        return returnTypeId;
    }

    @Nullable
    public String getFunctionalInterfaceId() {
        return functionalInterfaceId;
    }
}
