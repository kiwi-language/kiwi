package tech.metavm.flow.rest;

public record BranchDTO(
        Long id,
        Long ownerId,
        ValueDTO condition,
        ScopeDTO scope,
        boolean preselected
)  {
}
