package tech.metavm.flow.rest;

public record BranchDTO(
        Long id,
        long ownerId,
        ValueDTO condition,
        ScopeDTO scope
)  {
}
