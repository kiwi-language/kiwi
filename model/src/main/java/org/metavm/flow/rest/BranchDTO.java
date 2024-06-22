package org.metavm.flow.rest;

import org.metavm.common.rest.dto.BaseDTO;

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
