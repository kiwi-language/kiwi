package org.manul.object.instance.persistence;

public record VersionPO (
        long appId,
        long id,
        long version
) {

//    public Id getInstanceId() {
//        return Id.fromBytes(id);
//    }

}
