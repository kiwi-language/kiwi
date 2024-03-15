package tech.metavm.user.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LoginInfo(
        String appId,
        String userId
) {

    public static LoginInfo failed() {
        return new LoginInfo(null, null);
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return appId != null;
    }

}
