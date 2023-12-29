package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;

import java.util.List;

public record ScopeDTO(
        Long id,
        Long tmpId,
        List<NodeDTO> nodes
) implements BaseDTO {

}
