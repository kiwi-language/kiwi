package tech.metavm.object.type.websocket.dto;

public record MetaChangeMessage(
        long tenantId,
        long version
) {
}
