package org.metavm.flow.rest;

import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class LambdaNodeParam extends ScopeNodeParam implements Copyable<LambdaNodeParam> {

    private final List<ParameterDTO> parameters;
    private final String returnType;
    @Nullable
    private final String functionalInterface;

    public LambdaNodeParam(ScopeDTO bodyScope, List<ParameterDTO> parameters, String returnType,
                           @Nullable String functionalInterface) {
        super(bodyScope);
        this.parameters = parameters;
        this.returnType = returnType;
        this.functionalInterface = functionalInterface;
    }

    public List<ParameterDTO> getParameters() {
        return parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    @Nullable
    public String getFunctionalInterface() {
        return functionalInterface;
    }

    @Override
    public LambdaNodeParam copy(CopyContext context) {
        return new LambdaNodeParam(
                context.copy(getBodyScope()),
                NncUtils.map(getParameters(), context::copy),
                returnType,
                functionalInterface
        );
    }
}
