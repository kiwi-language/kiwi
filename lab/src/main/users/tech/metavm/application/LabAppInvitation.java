package tech.metavm.application;

import tech.metavm.entity.EntityIndex;
import tech.metavm.entity.EntityType;
import tech.metavm.user.LabPlatformUser;
import tech.metavm.utils.LabBusinessException;
import tech.metavm.utils.LabErrorCode;

@EntityType
public class LabAppInvitation {

    @EntityIndex
    public record IndexApplication(UserApplication application) {
        public IndexApplication(LabAppInvitation invitation) {
            this(invitation.application);
        }
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
