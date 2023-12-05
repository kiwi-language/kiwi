package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;

import java.util.List;

public record ScopeDTO(
        Long tmpId,
        Long id,
        List<NodeDTO> nodes
) implements BaseDTO {

}
