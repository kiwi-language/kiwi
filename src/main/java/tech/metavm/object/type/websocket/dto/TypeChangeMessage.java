package tech.metavm.object.type.websocket.dto;

public record TypeChangeMessage(
        long tenantId,
        long version
) {
}
