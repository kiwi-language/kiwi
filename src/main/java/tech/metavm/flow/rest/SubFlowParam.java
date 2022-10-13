package tech.metavm.flow.rest;

import java.util.List;

public record SubFlowParam (
        ValueDTO self,
        long flowId,
        List<FieldParamDTO> fields
) {
}
