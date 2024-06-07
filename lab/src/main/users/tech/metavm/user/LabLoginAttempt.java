package tech.metavm.user;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityIndex;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.Index;

import java.util.Date;

@EntityType
public record LabLoginAttempt(@EntityField("是否成功") boolean successful, @EntityField("账号") String loginName,
                              @EntityField("IP地址") String clientIP, @EntityField("时间") Date time) {

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
}
