package tech.metavm.application.rest.dto;

public record ApplicationDTO(
        long id,
        String name,
        long ownerId
) {
}
