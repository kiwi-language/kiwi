package org.metavm.user.rest.dto;

import org.metavm.user.Token;

public record LoginResponse(
        boolean successful,
        Token token
) {

}
