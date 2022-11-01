package tech.metavm.tenant.rest.dto;

public record TenantCreateRequest(
        String name,
        String rootPassword
) {

}
