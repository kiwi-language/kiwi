package org.metavm.application;

import org.metavm.api.EntityIndex;
import org.metavm.api.EntityType;
import org.metavm.api.Index;
import org.metavm.api.ValueType;
import org.metavm.user.LabPlatformUser;
import org.metavm.utils.LabBusinessException;
import org.metavm.utils.LabErrorCode;

@EntityType
public class LabAppInvitation {

    @ValueType
    public record IndexApplication(UserApplication application) implements Index<LabAppInvitation> {
    }

    private final UserApplication application;
    private final LabPlatformUser user;
    private final boolean isAdmin;
    private LabAppInvitationState state = LabAppInvitationState.INITIAL;

    public LabAppInvitation(UserApplication application, LabPlatformUser user, boolean isAdmin) {
        this.application = application;
        this.user = user;
        this.isAdmin = isAdmin;
    }

    @EntityIndex
    private IndexApplication indexApplication() {
        return new IndexApplication(application);
    }

    public UserApplication getApplication() {
        return application;
    }

    public LabPlatformUser getUser() {
        return user;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void accept() {
        if(this.state != LabAppInvitationState.INITIAL)
            throw new LabBusinessException(LabErrorCode.INVITATION_ALREADY_ACCEPTED);
        this.state = LabAppInvitationState.ACCEPTED;
    }

//    public AppInvitationDTO toDTO() {
//        return new AppInvitationDTO(user.getId(), application.getId(), getTitle(),
//                isAdmin, state.code());
//    }

    private String getTitle() {
        return String.format("You are invited to join application %s", application.getName());
    }

}
