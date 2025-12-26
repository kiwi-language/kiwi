package org.metavm.application;

import org.metavm.api.Entity;
import org.metavm.application.rest.dto.AppInvitationDTO;
import org.metavm.common.ErrorCode;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.*;
import org.metavm.user.PlatformUser;
import org.metavm.user.User;
import org.metavm.util.BusinessException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Wire(19)
@Entity
public class AppInvitation extends org.metavm.entity.Entity {

    public static final IndexDef<AppInvitation> IDX_APP = IndexDef.create(AppInvitation.class,
            1, appInvitation -> List.of(appInvitation.application)
            );

    public static AppInvitation create(AppInvitationDTO invitationDTO, IInstanceContext platformCtx) {
        var app = platformCtx.getEntity(Application.class, invitationDTO.appId());
        var user = platformCtx.getEntity(PlatformUser.class, invitationDTO.userId());
        return new AppInvitation(platformCtx.allocateRootId(), app, user, invitationDTO.isAdmin());
    }

    private final EntityReference application;
    private final EntityReference user;
    private final boolean isAdmin;
    private AppInvitationState state = AppInvitationState.INITIAL;

    public AppInvitation(Id id, Application application, User user, boolean isAdmin) {
        super(id);
        this.application = application.getReference();
        this.user = user.getReference();
        this.isAdmin = isAdmin;
    }

    public Application getApplication() {
        return (Application) application.get();
    }

    public User getUser() {
        return (User) user.get();
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

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(application);
        action.accept(user);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
