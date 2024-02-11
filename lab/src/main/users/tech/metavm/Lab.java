package tech.metavm;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityFlow;
import tech.metavm.entity.EntityType;
import tech.metavm.user.LabPlatformUser;
import tech.metavm.user.LabRole;
import tech.metavm.user.LabSession;
import tech.metavm.user.LabUser;
import tech.metavm.utils.LabBusinessException;
import tech.metavm.utils.LabErrorCode;

import java.util.Date;
import java.util.List;

@EntityType("实验室")
public class Lab {

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
    public void login(long appId, String loginName, String password, String clientIP) {
        var result = LabUser.login(appId, loginName, password, clientIP);
        if (result.token() == null)
            throw new LabBusinessException(LabErrorCode.LOGIN_FAILED);
    }

}
