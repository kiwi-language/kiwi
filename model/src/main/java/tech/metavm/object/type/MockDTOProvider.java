package tech.metavm.object.type;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.rest.dto.PTypeDTO;
import tech.metavm.object.type.rest.dto.ParameterizedFlowDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeKey;

import java.util.List;

public class MockDTOProvider implements DTOProvider {
    @Nullable
    @Override
    public Long getTmpId(TypeKey typeKey) {
        return null;
    }

    @Nullable
    @Override
    public PTypeDTO getPTypeDTO(String templateId, List<String> typeArgumentIds) {
        return null;
    }

    @Nullable
    @Override
    public ParameterizedFlowDTO getParameterizedFlowDTO(String templateId, List<String> typeArgumentIds) {
        return null;
    }

    @Nullable
    @Override
    public TypeDTO getTypeDTO(String ref) {
        return null;
    }
}
