package tech.metavm.flow.rest;

import java.util.List;

public record ScopeDTO(
    long id,
    List<NodeDTO> nodes
) {

}
