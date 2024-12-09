package org.metavm.user;

import org.metavm.api.EntityType;
import org.metavm.api.Index;
import org.metavm.api.ValueType;

import java.util.Date;

@EntityType
public record LabLoginAttempt(boolean successful, String loginName,
                              String clientIP, Date time) {

    public static final Index<LoginNameSuccTimeIndex, LabLoginAttempt> nameIndex =
            new Index<>(false, a -> new LoginNameSuccTimeIndex(a.loginName, a.successful, a.time));

    public static final Index<ClientIpSuccTimeIndex, LabLoginAttempt> ipIndex =
            new Index<>(false, a -> new ClientIpSuccTimeIndex(a.clientIP, a.successful, a.time));

    @ValueType
    public record LoginNameSuccTimeIndex(String loginName, boolean successful, Date time) {
    }

    @ValueType
    public record ClientIpSuccTimeIndex(String clientIP, boolean successful, Date time) {
    }

}
