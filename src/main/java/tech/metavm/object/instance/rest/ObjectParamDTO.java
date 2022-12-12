package tech.metavm.object.instance.rest;

import java.util.List;

public record ObjectParamDTO (
        List<InstanceFieldDTO> fields
) {
}
