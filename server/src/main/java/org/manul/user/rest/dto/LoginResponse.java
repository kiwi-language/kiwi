package org.manul.user.rest.dto;

import org.manul.user.Token;

public record LoginResponse(
        boolean successful,
        Token token
) {

}
