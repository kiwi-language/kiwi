package org.metavm.flow.rest;

import org.metavm.common.BaseDTO;

import java.util.List;

public record ScopeDTO(
        String id,
        List<NodeDTO> nodes
) implements BaseDTO {

}
