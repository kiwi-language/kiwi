package tech.metavm.object.type.rest.dto;

import java.util.List;

public record GetFunctionTypeRequest(
        List<String> parameterTypeIds,
        String returnTypeId
) {

}
