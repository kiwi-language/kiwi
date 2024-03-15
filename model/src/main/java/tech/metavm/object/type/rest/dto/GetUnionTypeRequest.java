package tech.metavm.object.type.rest.dto;

import java.util.List;

public record GetUnionTypeRequest(
        List<String> memberIds
) {

}
