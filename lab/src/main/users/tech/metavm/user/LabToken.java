package tech.metavm.user;

import tech.metavm.application.LabApplication;
import tech.metavm.entity.EntityType;

@EntityType(value = "令牌", ephemeral = true)
public record LabToken(LabApplication application, String token) {

}
