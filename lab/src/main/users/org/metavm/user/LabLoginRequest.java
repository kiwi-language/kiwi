package org.metavm.user;

import org.metavm.api.Entity;

@Entity(ephemeral = true)
public record LabLoginRequest(long appId, String loginName, String password) {

}
