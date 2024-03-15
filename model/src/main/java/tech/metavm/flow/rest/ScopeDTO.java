package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;

import java.util.List;

public record ScopeDTO(
        String id,
        List<NodeDTO> nodes
) implements BaseDTO {

}
