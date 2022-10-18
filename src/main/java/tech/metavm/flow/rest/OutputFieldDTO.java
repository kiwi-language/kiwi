package tech.metavm.flow.rest;

public record OutputFieldDTO(
        Long id,
        String name,
        long typeId,
        ValueDTO value
) {

}
