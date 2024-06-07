package tech.metavm.application;

import tech.metavm.application.rest.dto.AppInvitationDTO;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IndexDef;
import tech.metavm.user.PlatformUser;
import tech.metavm.user.User;
import tech.metavm.util.BusinessException;

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
        return String.format("您被邀请加入应用: %s", application.getName());
    }

}
