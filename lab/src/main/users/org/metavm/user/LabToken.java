package org.metavm.user;

import org.metavm.application.LabApplication;
import org.metavm.entity.EntityType;

@EntityType(ephemeral = true)
public record LabToken(LabApplication application, String token) {

}
