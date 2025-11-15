package org.metavm.user;

import org.jsonk.Json;

@Json
public record Token(
        long appId,
        String token
) {

}
