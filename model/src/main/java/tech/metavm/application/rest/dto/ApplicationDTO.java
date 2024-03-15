package tech.metavm.application.rest.dto;

public record ApplicationDTO(
        long id,
        String name,
        String ownerId
) {
}
