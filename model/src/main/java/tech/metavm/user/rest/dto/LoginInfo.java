package tech.metavm.user.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LoginInfo(
        long appId,
        String userId
) {

    public static LoginInfo failed() {
        return new LoginInfo(-1L, null);
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return appId != -1L;
    }

}
