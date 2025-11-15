package org.metavm.user.rest.dto;

import org.jsonk.Json;
import org.jsonk.JsonIgnore;

@Json
public record LoginInfo(
        long appId,
        String userId,
        String token
) {

    public static LoginInfo failed() {
        return new LoginInfo(-1L, null, null);
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return appId != -1L;
    }

}
