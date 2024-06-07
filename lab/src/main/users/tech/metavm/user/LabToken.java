package tech.metavm.user;

import tech.metavm.application.LabApplication;
import tech.metavm.entity.EntityType;

@EntityType(ephemeral = true)
public record LabToken(LabApplication application, String token) {

}
