package tech.metavm.flow.rest;

public record InputFieldDTO (
        Long id,
        String name,
        long typeId,
        Object defaultValue
){

}
