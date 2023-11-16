package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

import java.util.List;

public record FlowSignatureDTO(
        String name,
        List<RefDTO> parameterTypeRefs
) {
}
