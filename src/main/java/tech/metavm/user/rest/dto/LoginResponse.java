package tech.metavm.user.rest.dto;

import tech.metavm.user.Token;

public record LoginResponse(
        boolean successful,
        Token token
) {

}
