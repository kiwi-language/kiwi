package tech.metavm.object.type;

import org.jetbrains.annotations.Nullable;
import tech.metavm.common.RefDTO;
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
    public PTypeDTO getPTypeDTO(RefDTO templateRef, List<RefDTO> typeArgumentRefs) {
        return null;
    }

    @Nullable
    @Override
    public ParameterizedFlowDTO getParameterizedFlowDTO(RefDTO templateRef, List<RefDTO> typeArgumentRefs) {
        return null;
    }

    @Nullable
    @Override
    public TypeDTO getTypeDTO(RefDTO ref) {
        return null;
    }
}
