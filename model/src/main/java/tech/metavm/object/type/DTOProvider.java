package tech.metavm.object.type;

import tech.metavm.object.type.rest.dto.PTypeDTO;
import tech.metavm.object.type.rest.dto.ParameterizedFlowDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeKey;

import javax.annotation.Nullable;
import java.util.List;

public interface DTOProvider {

    @Nullable Long getTmpId(TypeKey typeKey);

    @Nullable
    PTypeDTO getPTypeDTO(String templateId, List<String> typeArgumentIds);

    @Nullable
    ParameterizedFlowDTO getParameterizedFlowDTO(String templateId, List<String> typeArgumentIds);

    @Nullable
    TypeDTO getTypeDTO(String ref);
}
