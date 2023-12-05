package tech.metavm.user.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LoginInfo(
        long appId,
        long userId
) {

    public static LoginInfo failed() {
        return new LoginInfo(-1L, -1L);
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return appId != -1L;
    }

}
