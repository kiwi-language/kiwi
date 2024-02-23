package tech.metavm;

import tech.metavm.application.LabApplication;
import tech.metavm.application.PlatformApplication;
import tech.metavm.application.UserApplication;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityFlow;
import tech.metavm.entity.EntityType;
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
    public UserApplication createApplication(String name, LabPlatformUser owner) {
        return new UserApplication(name, owner);
    }

    @EntityFlow("创建平台用户")
    public LabPlatformUser createPlatformUser(String loginName, String password, String name, List<LabRole> roles) {
        return new LabPlatformUser(loginName, password, name, roles);
    }

    @EntityFlow("发送验证码")
    public void sendVerificationCode(String receiver, String code, String clientIP) {
        LabVerificationCode.sendVerificationCode(receiver, code, clientIP);
    }

    @EntityFlow("创建会话")
    public LabSession createSession(LabUser user, Date autoCloseAt) {
        return new LabSession(user, autoCloseAt);
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
    public void logout(LabApplication application, String token) {
        LabUser.logout(List.of(new LabToken(application, token)));
    }

    @EntityFlow("加入应用")
    public void joinApp(LabPlatformUser user, LabApplication application) {
        LabPlatformUser.joinApplication(user, application);
    }

    @EntityFlow("离开应用")
    public void leaveApp(LabPlatformUser user, UserApplication application) {
        LabPlatformUser.leaveApp(List.of(user), application);
    }

    @EntityFlow("进入应用")
    public void enterApp(LabPlatformUser user, UserApplication application) {
        LabPlatformUser.enterApp(user, application);
    }

}
