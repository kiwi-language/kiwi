package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;

public record BranchDTO(
        String id,
        Long index,
        String ownerId,
        ValueDTO condition,
        ScopeDTO scope,
        boolean preselected,
        boolean isExit
)  implements BaseDTO {
}
