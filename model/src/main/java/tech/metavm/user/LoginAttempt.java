package tech.metavm.user;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;

import java.util.Date;

@EntityType("登录尝试")
public class LoginAttempt extends Entity {

    public static final IndexDef<LoginAttempt> IDX_LOGIN_NAME_SUCC_TIME =
            IndexDef.create(LoginAttempt.class, "loginName", "successful", "time");

    public static final IndexDef<LoginAttempt> IDX_CLIENT_IP_SUCC_TIME  =
            IndexDef.create(LoginAttempt.class, "clientIP", "successful", "time");

    @EntityField("是否成功")
    private final boolean successful;

    @EntityField("账号")
    private final String loginName;

    @EntityField("IP地址")
    private final String clientIP;

    @EntityField("时间")
    private final Date time;

    public LoginAttempt(boolean successful, String loginName, String clientIP, Date time) {
        this.successful = successful;
        this.loginName = loginName;
        this.clientIP = clientIP;
        this.time = time;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getClientIP() {
        return clientIP;
    }

    public Date getTime() {
        return time;
    }
}
