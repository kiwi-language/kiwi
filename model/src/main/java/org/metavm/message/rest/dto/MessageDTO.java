package org.metavm.message.rest.dto;

import javax.annotation.Nullable;

public record MessageDTO(
        String id,
        String receiverId,
        String title,
        int kind,
        @Nullable String targetId,
        boolean read
) {
}
