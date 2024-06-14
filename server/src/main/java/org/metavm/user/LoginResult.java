package org.metavm.user;

import javax.annotation.Nullable;

public record LoginResult(
        @Nullable Token token,
        String userId
) {
}
