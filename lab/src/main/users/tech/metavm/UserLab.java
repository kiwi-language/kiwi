package tech.metavm;

import tech.metavm.application.LabAppInvitation;
import tech.metavm.application.LabAppInvitationRequest;
import tech.metavm.application.LabApplication;
import tech.metavm.application.UserApplication;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.message.LabMessage;
import tech.metavm.user.*;

import java.util.List;

@EntityType
public class UserLab {

    @EntityField(asTitle = true)
    private String label;

    public LabRole createRole(String name) {
        return new LabRole(name);
    }

    public UserApplication createApplication(String name) {
        return UserApplication.create(name, LabPlatformUser.currentPlatformUser());
    }

    public LabPlatformUser createPlatformUser(String loginName, String password, String name, List<LabRole> roles) {
        return new LabPlatformUser(loginName, password, name, roles);
    }

    public void login(LabApplication application, String loginName, String password, String clientIP) {
        LabUser.login(application, loginName, password, clientIP);
    }

    public void verify(LabApplication application, String token) {
        LabUser.verify(new LabToken(application, token));
    }

    public void logout() {
        LabPlatformUser.logout();
    }

    public void leaveApp(UserApplication application) {
        LabPlatformUser.leaveApp(List.of(LabPlatformUser.currentPlatformUser()), application);
    }

    public void enterApp(UserApplication application) {
        LabPlatformUser.enterApp(LabPlatformUser.currentPlatformUser(), application);
    }

    public void invite(UserApplication application, LabPlatformUser user, boolean isAdmin) {
        UserApplication.invite(new LabAppInvitationRequest(
                application,
                user,
                isAdmin
        ));
    }

    public void acceptInvitation(LabAppInvitation invitation) {
        UserApplication.acceptInvitation(invitation);
    }

    public void sendVerificationCode(String receiver) {
        LabVerificationCode.sendVerificationCode(receiver, "MetaVM Verification Code", "127.0.0.1");
    }

    public void register(String loginName, String name, String password, String verificationCode) {
        LabPlatformUser.register(new LabRegisterRequest(loginName, name, password, verificationCode));
    }

    public void changePassword(String verificationCode, String loginName, String password) {
        LabPlatformUser.changePassword(new LabChangePasswordRequest(verificationCode, loginName, password));
    }

    public void evict(UserApplication app, List<LabPlatformUser> users) {
        UserApplication.evict(app, users);
    }

    public void promote(UserApplication app, LabPlatformUser user) {
        UserApplication.promote(app, user);
    }

    public void demote(UserApplication app, LabPlatformUser user) {
        UserApplication.demote(app, user);
    }

    public void readMessage(LabMessage message) {
        LabMessage.read(message);
    }

}
