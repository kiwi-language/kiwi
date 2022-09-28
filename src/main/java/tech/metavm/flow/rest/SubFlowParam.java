package tech.metavm.flow.rest;

import java.util.List;

public record SubFlowParam (
        ValueDTO selfId,
        long flowId,
        List<FieldParamDTO> fieldParams
) {
}
