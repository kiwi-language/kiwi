package org.metavm.object.type;

import org.metavm.object.type.rest.dto.TypeDTO;

import javax.annotation.Nullable;

public interface DTOProvider {

    @Nullable
    TypeDTO getTypeDTO(String ref);
}
