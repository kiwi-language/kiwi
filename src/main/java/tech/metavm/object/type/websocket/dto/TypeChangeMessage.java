package tech.metavm.object.type.websocket.dto;

import javax.annotation.Nullable;
import java.util.List;

public record TypeChangeMessage(
        long tenantId,
        long version,
        List<Long> typeIds,
        @Nullable String triggeringClientId
) {
}
