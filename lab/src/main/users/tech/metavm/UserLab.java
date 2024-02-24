package tech.metavm;

import tech.metavm.application.*;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityFlow;
import tech.metavm.entity.EntityType;
import tech.metavm.message.LabMessage;
import tech.metavm.user.*;

import java.util.Date;
import java.util.List;

@EntityType("实验室")
public class UserLab {

    @EntityField(value = "标签", asTitle = true)
    private String label;

    @EntityFlow("创建角色")
    public LabRole createRole(String name) {
        return new LabRole(name);
    }

    @EntityFlow("创建应用")
    public UserApplication createApplication(String name) {
        return UserApplication.create(name, LabPlatformUser.currentPlatformUser());
    }

    @EntityFlow("创建平台用户")
    public LabPlatformUser createPlatformUser(String loginName, String password, String name, List<LabRole> roles) {
        return new LabPlatformUser(loginName, password, name, roles);
    }

    @EntityFlow("登录")
    public void login(LabApplication application, String loginName, String password, String clientIP) {
        LabUser.login(application, loginName, password, clientIP);
    }

    @EntityFlow("验证")
    public void verify(LabApplication application, String token) {
        LabUser.verify(new LabToken(application, token));
    }

    @EntityFlow("登出")
    public void logout() {
        LabPlatformUser.logout();
    }

    @EntityFlow("离开应用")
    public void leaveApp(UserApplication application) {
        LabPlatformUser.leaveApp(List.of(LabPlatformUser.currentPlatformUser()), application);
    }

    @EntityFlow("进入应用")
    public void enterApp(UserApplication application) {
        LabPlatformUser.enterApp(LabPlatformUser.currentPlatformUser(), application);
    }

    @EntityFlow("邀请")
    public void invite(UserApplication application, LabPlatformUser user, boolean isAdmin) {
        UserApplication.invite(new LabAppInvitationRequest(
                application,
                user,
                isAdmin
        ));
    }

    @EntityFlow("接受邀请")
    public void acceptInvitation(LabAppInvitation invitation) {
        UserApplication.acceptInvitation(invitation);
    }

    @EntityFlow("发送验证码")
    public void sendVerificationCode(String receiver) {
        LabVerificationCode.sendVerificationCode(receiver, "MetaVM注册验证码", "127.0.0.1");
    }

    @EntityFlow("注册")
    public void register(String loginName, String name, String password, String verificationCode) {
        LabPlatformUser.register(new LabRegisterRequest(loginName, name, password, verificationCode));
    }

    @EntityFlow("修改密码")
    public void changePassword(String verificationCode, String loginName, String password) {
        LabPlatformUser.changePassword(new LabChangePasswordRequest(verificationCode, loginName, password));
    }

    @EntityFlow("移除用户")
    public void evict(UserApplication app, List<LabPlatformUser> users) {
        UserApplication.evict(app, users);
    }

    @EntityFlow("晋升管理员")
    public void promote(UserApplication app, LabPlatformUser user) {
        UserApplication.promote(app, user);
    }

    @EntityFlow("撤销管理员")
    public void demote(UserApplication app, LabPlatformUser user) {
        UserApplication.demote(app, user);
    }

    @EntityFlow("标记消息已读")
    public void readMessage(LabMessage message) {
        LabMessage.read(message);
    }

}
