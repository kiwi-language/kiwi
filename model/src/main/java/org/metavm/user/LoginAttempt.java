package org.metavm.user;

import org.metavm.entity.Entity;
import org.metavm.api.EntityType;
import org.metavm.entity.IndexDef;

import java.util.Date;

@EntityType
public class LoginAttempt extends Entity {

    public static final IndexDef<LoginAttempt> IDX_LOGIN_NAME_SUCC_TIME =
            IndexDef.create(LoginAttempt.class, "loginName", "successful", "time");

    public static final IndexDef<LoginAttempt> IDX_CLIENT_IP_SUCC_TIME  =
            IndexDef.create(LoginAttempt.class, "clientIP", "successful", "time");

    private final boolean successful;

    private final String loginName;

    private final String clientIP;

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
