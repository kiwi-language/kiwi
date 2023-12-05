package tech.metavm.system.rest.dto;

public record BlockDTO(
        long id,
        long appId,
        long typeId,
        long start,
        long end,
        long next,
        boolean active
) {
}
