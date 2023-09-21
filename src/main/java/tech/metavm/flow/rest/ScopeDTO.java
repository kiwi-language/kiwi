package tech.metavm.flow.rest;

import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

public record ScopeDTO(
        Long tmpId,
        Long id,
        List<NodeDTO> nodes
) implements BaseDTO {

}
