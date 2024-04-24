package tech.metavm.object.type.rest.dto;

public record CapturedTypeVariableDTO(String id, String uncertainType, String scopeId, int index) implements TypeDefDTO {
}
