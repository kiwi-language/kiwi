package org.metavm.user;

import org.metavm.api.EntityType;

@EntityType(ephemeral = true)
public record LabLoginRequest(long appId, String loginName, String password) {

}
