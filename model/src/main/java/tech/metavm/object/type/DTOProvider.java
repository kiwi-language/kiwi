package tech.metavm.object.type;

import tech.metavm.object.type.rest.dto.TypeDTO;

import javax.annotation.Nullable;

public interface DTOProvider {

    @Nullable
    TypeDTO getTypeDTO(String ref);
}
