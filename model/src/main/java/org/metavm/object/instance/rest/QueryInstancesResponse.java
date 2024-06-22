package org.metavm.object.instance.rest;

import org.metavm.common.Page;
import org.metavm.object.type.rest.dto.KlassDTO;

import java.util.List;

public record QueryInstancesResponse(
        Page<InstanceDTO> page,
        List<KlassDTO> contextTypes
) {
}
