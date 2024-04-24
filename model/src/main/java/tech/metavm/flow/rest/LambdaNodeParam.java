package tech.metavm.flow.rest;

import javax.annotation.Nullable;
import java.util.List;

public class LambdaNodeParam extends ScopeNodeParamDTO {

    private final List<ParameterDTO> parameters;
    private final String returnTypeId;
    @Nullable
    private final String functionalInterface;

    public LambdaNodeParam(ScopeDTO bodyScope, List<ParameterDTO> parameters, String returnTypeId,
                           @Nullable String functionalInterface) {
        super(bodyScope);
        this.parameters = parameters;
        this.returnTypeId = returnTypeId;
        this.functionalInterface = functionalInterface;
    }

    public List<ParameterDTO> getParameters() {
        return parameters;
    }

    public String getReturnTypeId() {
        return returnTypeId;
    }

    @Nullable
    public String getFunctionalInterface() {
        return functionalInterface;
    }
}
