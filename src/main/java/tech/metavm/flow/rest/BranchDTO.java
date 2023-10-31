package tech.metavm.flow.rest;

import tech.metavm.dto.BaseDTO;

public record BranchDTO(
        Long id,
        Long tmpId,
        Long index,
        Long ownerId,
        ValueDTO condition,
        ScopeDTO scope,
        boolean preselected,
        boolean isExit
)  implements BaseDTO {
}
