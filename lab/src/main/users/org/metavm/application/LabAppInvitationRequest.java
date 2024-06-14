package org.metavm.application;

import org.metavm.entity.ValueStruct;
import org.metavm.user.LabPlatformUser;

@ValueStruct
public record LabAppInvitationRequest(
        UserApplication application,
        LabPlatformUser user,
        boolean isAdmin
) {
}
