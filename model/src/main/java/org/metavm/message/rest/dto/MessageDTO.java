package org.metavm.message.rest.dto;

import org.jsonk.Json;

import javax.annotation.Nullable;

@Json
public record MessageDTO(
        String id,
        String receiverId,
        String title,
        int kind,
        @Nullable String targetId,
        boolean read
) {
}
