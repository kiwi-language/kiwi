package org.metavm.user;

import org.metavm.api.EntityIndex;
import org.metavm.api.EntityType;
import org.metavm.api.Index;
import org.metavm.api.ValueType;

import java.util.Date;

@EntityType
public record LabLoginAttempt(boolean successful, String loginName,
                              String clientIP, Date time) {

    @ValueType
    public record LoginNameSuccTimeIndex(String loginName, boolean successful, Date time)
            implements Index<LabLoginAttempt> {
    }

    @ValueType
    public record ClientIpSuccTimeIndex(String clientIP, boolean successful, Date time)
            implements Index<LabLoginAttempt> {
    }

    @EntityIndex
    private LoginNameSuccTimeIndex loginNameSuccTimeIndex() {
        return new LoginNameSuccTimeIndex(loginName, successful, time);
    }

    @EntityIndex
    private ClientIpSuccTimeIndex clientIpSuccTimeIndex() {
        return new ClientIpSuccTimeIndex(clientIP, successful, time);
    }

}
