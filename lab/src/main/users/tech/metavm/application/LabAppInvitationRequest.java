package tech.metavm.application;

import tech.metavm.entity.EntityType;
import tech.metavm.user.LabPlatformUser;

@EntityType(ephemeral = true)
public record LabAppInvitationRequest(
        UserApplication application,
        LabPlatformUser user,
        boolean isAdmin
) {
}
