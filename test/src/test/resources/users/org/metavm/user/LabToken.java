package org.metavm.user;

import org.metavm.application.LabApplication;
import org.metavm.api.Entity;

@Entity(ephemeral = true)
public record LabToken(LabApplication application, String token) {

}
