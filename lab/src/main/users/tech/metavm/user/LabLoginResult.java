package tech.metavm.user;

import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType(ephemeral = true)
public record LabLoginResult(@Nullable String token, LabUser user) {

}
