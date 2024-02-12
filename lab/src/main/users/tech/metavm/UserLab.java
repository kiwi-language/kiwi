package tech.metavm;

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

    @EntityFlow("创建平台用户")
    public LabPlatformUser createPlatformUser(String loginName, String password, String name, List<LabRole> roles) {
        return new LabPlatformUser(loginName, password, name, roles);
    }

    @EntityFlow("创建会话")
    public LabSession createSession(LabUser user, Date autoCloseAt) {
        return new LabSession(user, autoCloseAt);
    }

    @EntityFlow("登录")
    public void login(String loginName, String password, String clientIP) {
        LabUser.login(1L, loginName, password, clientIP);
    }

    @EntityFlow("验证")
    public void verify(String token) {
        LabUser.verify(new LabToken(1L, token));
    }

    @EntityFlow("登出")
    public void logout(String token) {
        LabUser.logout(List.of(new LabToken(1L, token)));
    }

}
