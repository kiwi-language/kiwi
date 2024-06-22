package org.metavm.object.type;

import org.jetbrains.annotations.Nullable;
import org.metavm.object.type.rest.dto.KlassDTO;

public class MockDTOProvider implements DTOProvider {

    @Nullable
    @Override
    public KlassDTO getTypeDTO(String ref) {
        return null;
    }
}
