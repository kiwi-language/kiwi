package tech.metavm.message.rest.dto;

import javax.annotation.Nullable;

public record MessageDTO(
        long id,
        long receiverId,
        String title,
        int kind,
        @Nullable Long targetId,
        boolean read
) {
}
