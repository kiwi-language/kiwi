package org.metavm.flow.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.List;

public class MethodCallNodeParam extends CallNodeParam {

    public MethodCallNodeParam(@JsonProperty("flowRef") MethodRefDTO flowRef,
                               @JsonProperty("flowCode") String flowCode,
                               @JsonProperty("typeArgumentIds") List<String> typeArgumentIds,
                               @JsonProperty("type") @Nullable String type,
                               @JsonProperty("capturedVariableTypes") List<String> capturedVariableTypes,
                               @JsonProperty("capturedVariableIndexes") List<Long> capturedVariableIndexes
    ) {
        super(flowRef, flowCode, typeArgumentIds, type, capturedVariableTypes, capturedVariableIndexes);
    }


    @Nullable
    @Override
    public MethodRefDTO getFlowRef() {
        return (MethodRefDTO) super.getFlowRef();
    }

    @Override
    public int getCallKind() {
        return 2;
    }

}
