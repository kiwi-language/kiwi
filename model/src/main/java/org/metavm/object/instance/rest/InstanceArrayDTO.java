package org.metavm.object.instance.rest;

import java.util.List;

public record InstanceArrayDTO(
        Long id,
        Long typeId,
        List<Object> elements
) {

}
