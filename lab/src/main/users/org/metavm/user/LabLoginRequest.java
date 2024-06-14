package org.metavm.user;

import org.metavm.entity.EntityType;

@EntityType(ephemeral = true)
public record LabLoginRequest(long appId, String loginName, String password) {

}
