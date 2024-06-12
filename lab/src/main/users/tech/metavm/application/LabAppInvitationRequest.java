package tech.metavm.application;

import tech.metavm.entity.ValueStruct;
import tech.metavm.user.LabPlatformUser;

@ValueStruct
public record LabAppInvitationRequest(
        UserApplication application,
        LabPlatformUser user,
        boolean isAdmin
) {
}
