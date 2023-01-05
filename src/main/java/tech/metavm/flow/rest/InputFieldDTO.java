package tech.metavm.flow.rest;

import tech.metavm.object.instance.rest.FieldValueDTO;

public record InputFieldDTO (
        Long id,
        String name,
        long typeId,
        FieldValueDTO defaultValue
){

}
