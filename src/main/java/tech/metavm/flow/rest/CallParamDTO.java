package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class CallParamDTO{
    private final RefDTO flowRef;
    @Nullable
    private final RefDTO typeRef;
    private final List<FieldParamDTO> fields;

    public CallParamDTO(RefDTO flowRef, @Nullable RefDTO typeRef, List<FieldParamDTO> fields) {
        this.flowRef = flowRef;
        this.typeRef = typeRef;
        this.fields = fields;
    }


    public RefDTO getFlowRef() {
        return flowRef;
    }

    @Nullable
    public RefDTO getTypeRef() {
        return typeRef;
    }

    public List<FieldParamDTO> getFields() {
        return fields;
    }
}
