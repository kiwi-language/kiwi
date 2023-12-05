package tech.metavm.object.instance.persistence;

public record VersionPO (
        long appId,
        long id,
        long version
) {
}
