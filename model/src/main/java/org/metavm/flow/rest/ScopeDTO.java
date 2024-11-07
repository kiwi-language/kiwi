package org.metavm.flow.rest;

import org.metavm.common.rest.dto.BaseDTO;

import java.util.List;

public record ScopeDTO(
        String id,
        List<NodeDTO> nodes,
        int maxLocals,
        int maxStack
) implements BaseDTO {

}
