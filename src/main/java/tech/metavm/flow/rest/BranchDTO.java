package tech.metavm.flow.rest;

import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;

public record BranchDTO(
        Long id,
        Long tmpId,
        Long index,
        Long ownerId,
        ValueDTO condition,
        ScopeDTO scope,
        boolean preselected
)  implements BaseDTO {
}
