package tech.metavm.object.type.rest.dto;

import java.util.List;

public record LoadAllTypesResponse(
    long metaVersion,
    List<TypeDTO> types
) {

}
