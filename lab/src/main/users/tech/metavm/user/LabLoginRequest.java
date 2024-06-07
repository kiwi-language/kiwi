package tech.metavm.user;

import tech.metavm.entity.EntityType;

@EntityType(ephemeral = true)
public record LabLoginRequest(long appId, String loginName, String password) {

}
