package tech.metavm.object.instance.persistence;

public record VersionPO (
        long tenantId,
        long id,
        long version
) {
}
