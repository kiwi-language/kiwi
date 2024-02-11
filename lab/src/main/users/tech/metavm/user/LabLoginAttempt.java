package tech.metavm.user;

import tech.metavm.builtin.IndexDef;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityIndex;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.Index;

import java.util.Date;

@EntityType("登录尝试")
public class LabLoginAttempt {

    public static final IndexDef<LabLoginAttempt> IDX_LOGIN_NAME_SUCC_TIME =
            IndexDef.create(LabLoginAttempt.class, "loginName", "successful", "time");

    public static final IndexDef<LabLoginAttempt> IDX_CLIENT_IP_SUCC_TIME  =
            IndexDef.create(LabLoginAttempt.class, "clientIP", "successful", "time");

    @EntityField("是否成功")
    private final boolean successful;

    @EntityField("账号")
    private final String loginName;

    @EntityField("IP地址")
    private final String clientIP;

    @EntityField("时间")
    private final Date time;

    public LabLoginAttempt(boolean successful, String loginName, String clientIP, Date time) {
        this.successful = successful;
        this.loginName = loginName;
        this.clientIP = clientIP;
        this.time = time;
    }

    @EntityIndex("登录名")
    public record LoginNameSuccTimeIndex(String loginName, boolean successful, Date time)
            implements Index<LabLoginAttempt> {

        public LoginNameSuccTimeIndex(LabLoginAttempt labLoginAttempt) {
            this(labLoginAttempt.loginName, labLoginAttempt.successful, labLoginAttempt.time);
        }
    }

    @EntityIndex("ID地址")
    public record ClientIpSuccTimeIndex(String clientIP, boolean successful, Date time)
            implements Index<LabLoginAttempt> {

        public ClientIpSuccTimeIndex(LabLoginAttempt labLoginAttempt) {
            this(labLoginAttempt.clientIP, labLoginAttempt.successful, labLoginAttempt.time);
        }
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
