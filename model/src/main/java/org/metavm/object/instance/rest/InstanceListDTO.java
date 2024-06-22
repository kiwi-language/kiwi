package org.metavm.object.instance.rest;

import org.metavm.object.type.rest.dto.KlassDTO;

import java.util.List;

public record InstanceListDTO (
        KlassDTO type,
        List<InstanceDTO> instances,
        long total
) {

}
