package org.metavm.object.type;

import org.metavm.object.type.rest.dto.KlassDTO;

import javax.annotation.Nullable;

public interface DTOProvider {

    @Nullable
    KlassDTO getTypeDTO(String ref);
}
