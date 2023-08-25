package tech.metavm.flow.rest;

import java.util.List;

public record ScopeDTO(
    Long id,
    List<NodeDTO> nodes
) {

}
