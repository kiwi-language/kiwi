package tech.metavm.user;

import javax.annotation.Nullable;

public record LoginResult(
        @Nullable Token token,
        long userId
) {
}
