package org.metavm.object.instance.rest;

import org.metavm.object.type.rest.dto.TypeDTO;

import java.util.List;

public record InstanceListDTO (
        TypeDTO type,
        List<InstanceDTO> instances,
        long total
) {

}
