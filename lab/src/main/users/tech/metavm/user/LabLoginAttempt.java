package tech.metavm.user;

import tech.metavm.entity.EntityIndex;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.Index;

import java.util.Date;

@EntityType
public record LabLoginAttempt(boolean successful, String loginName,
                              String clientIP, Date time) {

    @EntityIndex
    public record LoginNameSuccTimeIndex(String loginName, boolean successful, Date time)
            implements Index<LabLoginAttempt> {

        public LoginNameSuccTimeIndex(LabLoginAttempt labLoginAttempt) {
            this(labLoginAttempt.loginName, labLoginAttempt.successful, labLoginAttempt.time);
        }
    }

    @EntityIndex
    public record ClientIpSuccTimeIndex(String clientIP, boolean successful, Date time)
            implements Index<LabLoginAttempt> {

        public ClientIpSuccTimeIndex(LabLoginAttempt labLoginAttempt) {
            this(labLoginAttempt.clientIP, labLoginAttempt.successful, labLoginAttempt.time);
        }
    }
}
