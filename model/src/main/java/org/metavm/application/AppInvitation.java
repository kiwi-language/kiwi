package org.metavm.application;

import org.metavm.application.rest.dto.AppInvitationDTO;
import org.metavm.common.ErrorCode;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.IndexDef;
import org.metavm.user.PlatformUser;
import org.metavm.user.User;
import org.metavm.util.BusinessException;

@EntityType
public class AppInvitation extends Entity {

    public static final IndexDef<AppInvitation> IDX_APP = IndexDef.create(AppInvitation.class, "application");

    public static AppInvitation create(AppInvitationDTO invitationDTO, IEntityContext platformCtx) {
        var app = platformCtx.getEntity(Application.class, invitationDTO.appId());
        var user = platformCtx.getEntity(PlatformUser.class, invitationDTO.userId());
        return new AppInvitation(app, user, invitationDTO.isAdmin());
    }

    private final Application application;
    private final User user;
    private final boolean isAdmin;
    private AppInvitationState state = AppInvitationState.INITIAL;

    public AppInvitation(Application application, User user, boolean isAdmin) {
        this.application = application;
        this.user = user;
        this.isAdmin = isAdmin;
    }

    public Application getApplication() {
        return application;
    }

    public User getUser() {
        return user;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void accept() {
        if(this.state != AppInvitationState.INITIAL)
            throw new BusinessException(ErrorCode.INVITATION_ALREADY_ACCEPTED);
        this.state = AppInvitationState.ACCEPTED;
    }

    public AppInvitationDTO toDTO() {
        return new AppInvitationDTO(user.getStringId(), application.getStringId(), getTitle(),
                isAdmin, state.code());
    }

    private String getTitle() {
        return String.format("You are invited to join application %s", application.getName());
    }

}
