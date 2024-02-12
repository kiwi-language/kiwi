package tech.metavm.user;

import tech.metavm.entity.EntityType;

@EntityType(value = "登录请求", ephemeral = true)
public record LabLoginRequest(long appId, String loginName, String password) {

}
