package tech.metavm.object.type.rest.dto;

public record CapturedTypeKey(
        String scopeId,
        String uncertainTypeKey,
        int index
) implements TypeKey {
}
