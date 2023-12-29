package tech.metavm.object.type;

import tech.metavm.common.RefDTO;
import tech.metavm.object.type.rest.dto.PTypeDTO;
import tech.metavm.object.type.rest.dto.ParameterizedFlowDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeKey;

import javax.annotation.Nullable;
import java.util.List;

public interface DTOProvider {

    @Nullable Long getTmpId(TypeKey typeKey);

    @Nullable
    PTypeDTO getPTypeDTO(RefDTO templateRef, List<RefDTO> typeArgumentRefs);

    @Nullable
    ParameterizedFlowDTO getParameterizedFlowDTO(RefDTO templateRef, List<RefDTO> typeArgumentRefs);

    @Nullable
    TypeDTO getTypeDTO(RefDTO ref);
}
