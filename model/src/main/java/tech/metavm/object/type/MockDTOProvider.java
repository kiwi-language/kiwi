package tech.metavm.object.type;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeKey;

public class MockDTOProvider implements DTOProvider {

    @Nullable
    @Override
    public TypeDTO getTypeDTO(String ref) {
        return null;
    }
}
