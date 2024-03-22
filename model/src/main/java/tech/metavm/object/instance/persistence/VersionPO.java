package tech.metavm.object.instance.persistence;

import tech.metavm.object.instance.core.Id;

public record VersionPO (
        long appId,
        byte[] id,
        long version
) {

    public Id getInstanceId() {
        return Id.fromBytes(id);
    }

}
