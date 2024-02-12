package tech.metavm.user;

import tech.metavm.entity.EntityType;

@EntityType(value = "令牌", ephemeral = true)
public record LabToken(long appId, String token) {

}
