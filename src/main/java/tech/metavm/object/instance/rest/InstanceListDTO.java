package tech.metavm.object.instance.rest;

import tech.metavm.object.meta.rest.dto.TypeDTO;

import java.util.List;

public record InstanceListDTO (
        TypeDTO type,
        List<InstanceDTO> instances,
        long total
) {

}
