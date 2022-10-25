package tech.metavm.flow.rest;

public record OutputFieldDTO(
        Long id,
        String name,
        long typeId,
        ValueDTO value
) {

    public OutputFieldDTO copyWithId(long id) {
        return new OutputFieldDTO(
                id,
                name,
                typeId,
                value
        );
    }

}
