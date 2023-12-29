package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public class LambdaNodeParam extends ScopeNodeParamDTO {

    private final List<ParameterDTO> parameters;
    private final RefDTO returnTypeRef;
    @Nullable
    private final RefDTO functionalInterfaceRef;

    public LambdaNodeParam(ScopeDTO bodyScope, List<ParameterDTO> parameters, RefDTO returnTypeRef,
                           @Nullable RefDTO functionalInterfaceRef) {
        super(bodyScope);
        this.parameters = parameters;
        this.returnTypeRef = returnTypeRef;
        this.functionalInterfaceRef = functionalInterfaceRef;
    }

    public List<ParameterDTO> getParameters() {
        return parameters;
    }

    public RefDTO getReturnTypeRef() {
        return returnTypeRef;
    }

    @Nullable
    public RefDTO getFunctionalInterfaceRef() {
        return functionalInterfaceRef;
    }
}
