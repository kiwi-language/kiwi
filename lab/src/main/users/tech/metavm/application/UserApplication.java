package tech.metavm.application;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.lang.SystemUtils;
import tech.metavm.message.LabMessage;
import tech.metavm.message.LabMessageKind;
import tech.metavm.user.LabPlatformUser;
import tech.metavm.user.LabUser;
import tech.metavm.utils.LabBusinessException;
import tech.metavm.utils.LabErrorCode;
import tech.metavm.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;

@EntityType
public class UserApplication extends LabApplication {

    public static final int MAX_NUM_ADMINS = 16;

    private LabPlatformUser owner;

    @ChildEntity
    private final List<LabPlatformUser> admins = new ArrayList<>();

    private LabApplicationState state;

    public UserApplication(String name, LabPlatformUser owner) {
        super(name);
        this.owner = owner;
        this.admins.add(owner);
        state = LabApplicationState.ACTIVE;
    }

    public void setOwner(LabPlatformUser owner) {
        if (owner != this.owner) {
            this.owner = owner;
            addAdmin(owner);
        }
    }

    public LabPlatformUser getOwner() {
        return owner;
    }

    public void addAdmin(LabPlatformUser user) {
        if (!this.admins.contains(user)) {
            if (this.admins.size() >= MAX_NUM_ADMINS)
                throw new LabBusinessException(LabErrorCode.REENTERING_APP);
            this.admins.add(user);
        } else
            throw new LabBusinessException(LabErrorCode.ALREADY_AN_ADMIN, user.getName());
    }

    public void removeAdmin(LabPlatformUser user) {
        if (!removeAdminIfPresent(user))
            throw new LabBusinessException(LabErrorCode.USER_NOT_ADMIN, user.getName());
    }

    public boolean removeAdminIfPresent(LabPlatformUser user) {
        return this.admins.remove(user);
    }

    public LabApplicationState getState() {
        return state;
    }

    public List<LabPlatformUser> getAdmins() {
        return admins;
    }

    public boolean isAdmin(LabPlatformUser user) {
        return admins.contains(user);
    }

    public boolean isOwner(LabPlatformUser user) {
        return this.owner == user;
    }

    public void deactivate() {
        this.state = LabApplicationState.REMOVING;
    }

    public boolean isActive() {
        return state == LabApplicationState.ACTIVE;
    }

    public static UserApplication create(String name, LabPlatformUser owner) {
        var application = new UserApplication(name, owner);
        LabPlatformUser.joinApplication(owner, application);
        return application;
    }


    public static LabAppInvitation invite(LabAppInvitationRequest request) {
        SystemUtils.print("Current application: " + LabUser.currentApplication().getName());
        SystemUtils.print("Current user: " + LabUser.currentUser(LabUser.currentApplication()));
        SystemUtils.print("Current platform user: " + LabUser.currentUser(PlatformApplication.getInstance()));

        var app = request.application();
        ensureAppAdmin(app);
        var invitee = request.user();
        if (invitee.hasJoinedApplication(app))
            throw new LabBusinessException(LabErrorCode.ALREADY_JOINED_APP, invitee.getLoginName());
        var currentUser = LabPlatformUser.currentPlatformUser();
        var invitation = new LabAppInvitation(app, invitee, request.isAdmin());
        new LabMessage(
                invitee, String.format("'%s'邀请您加入应用'%s'", currentUser.getName(), app.getName()),
                LabMessageKind.INVITATION, invitation);
        return invitation;
    }

    public static void acceptInvitation(LabAppInvitation invitation) {
        var user = LabPlatformUser.currentPlatformUser();
        if (invitation.getUser() != user)
            throw new LabBusinessException(LabErrorCode.ILLEGAL_ACCESS);
        invitation.accept();
        LabPlatformUser.joinApplication(user, invitation.getApplication());
    }

    public static void evict(UserApplication app, List<LabPlatformUser> users) {
        ensureAppAdmin(app);
        for (var user : users) {
            if (app.isOwner(user))
                throw new LabBusinessException(LabErrorCode.CAN_NOT_EVICT_APP_OWNER);
            user.leaveApplication(app);
        }
    }

    public static void promote(UserApplication app, LabPlatformUser user) {
        ensureAppAdmin(app);
        app.addAdmin(user);
        new LabMessage(user, String.format("您已成为应用'%s'的管理员", app.getName()),
                LabMessageKind.DEFAULT, null);
    }

    public static void demote(UserApplication app, LabPlatformUser user) {
        ensureAppAdmin(app);
        app.removeAdmin(user);
        new LabMessage(user, String.format("您不再是应用'%s'的管理员", app.getName()),
                LabMessageKind.DEFAULT, null);
    }

    private static void ensureAppAdmin(UserApplication application) {
        if (UserUtils.nonMatch(application.getAdmins(), admin -> admin == LabPlatformUser.currentPlatformUser()))
            throw new LabBusinessException(LabErrorCode.CURRENT_USER_NOT_APP_ADMIN);
    }

    private static void ensureAppOwner(UserApplication application) {
        if (application.getOwner() != LabPlatformUser.currentPlatformUser())
            throw new LabBusinessException(LabErrorCode.CURRENT_USER_NOT_APP_OWNER);
    }

}
