package tech.metavm.system.rest.dto;

public record BlockDTO(
        long id,
        long appId,
        int typeTag,
        long typeId,
        long start,
        long end,
        long next,
        boolean active
) {
}
