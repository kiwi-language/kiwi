package org.metavm.user;

import org.metavm.api.Entity;
import org.metavm.api.Index;
import org.metavm.api.Value;

import java.util.Date;

@Entity
public record LabLoginAttempt(boolean successful, String loginName,
                              String clientIP, Date time) {

    public static final Index<LoginNameSuccTimeIndex, LabLoginAttempt> nameIndex =
            new Index<>(false, a -> new LoginNameSuccTimeIndex(a.loginName, a.successful, a.time));

    public static final Index<ClientIpSuccTimeIndex, LabLoginAttempt> ipIndex =
            new Index<>(false, a -> new ClientIpSuccTimeIndex(a.clientIP, a.successful, a.time));

    @Value
    public record LoginNameSuccTimeIndex(String loginName, boolean successful, Date time) {
    }

    @Value
    public record ClientIpSuccTimeIndex(String clientIP, boolean successful, Date time) {
    }

}
