package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class MethodCallNodeParam extends CallNodeParam implements Copyable<MethodCallNodeParam> {

    private final ValueDTO self;

    public MethodCallNodeParam(@JsonProperty("self") ValueDTO self,
                               @JsonProperty("flowRef") MethodRefDTO flowRef,
                               @JsonProperty("flowCode") String flowCode,
                               @JsonProperty("typeArgumentIds") List<String> typeArgumentIds,
                               @JsonProperty("type") @Nullable String type,
                               @JsonProperty("arguments") List<ArgumentDTO> arguments,
                               @JsonProperty("argumentValues") List<ValueDTO> argumentValues,
                               @JsonProperty("capturedExpressionTypes") List<String> capturedExpressionTypes,
                               @JsonProperty("capturedExpressions") List<String> capturedExpressions
    ) {
        super(flowRef, flowCode, typeArgumentIds, type, arguments, argumentValues, capturedExpressionTypes, capturedExpressions);
        this.self = self;
    }


    @Nullable
    @Override
    public MethodRefDTO getFlowRef() {
        return (MethodRefDTO) super.getFlowRef();
    }

    public ValueDTO getSelf() {
        return self;
    }

    @Override
    public int getCallKind() {
        return 2;
    }

    @Override
    public MethodCallNodeParam copy(CopyContext context) {
        return new MethodCallNodeParam(
                context.copy(self),
                context.copy(getFlowRef()),
                getFlowCode(),
                getTypeArguments(),
                getType(),
                NncUtils.map(getArguments(), context::copy),
                NncUtils.map(getArgumentValues(), context::copy),
                getCapturedExpressionTypes(),
                getCapturedExpressions()
        );
    }
}
