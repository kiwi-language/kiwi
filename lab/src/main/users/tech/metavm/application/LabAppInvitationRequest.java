package tech.metavm.application;

import tech.metavm.entity.EntityType;
import tech.metavm.user.LabPlatformUser;

@EntityType(value = "应用邀请请求", ephemeral = true)
public record LabAppInvitationRequest(
        UserApplication application,
        LabPlatformUser user,
        boolean isAdmin
) {
}
