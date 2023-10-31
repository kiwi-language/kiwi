package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record GetUnionTypeRequest(
        List<Long> memberIds
) {

}
