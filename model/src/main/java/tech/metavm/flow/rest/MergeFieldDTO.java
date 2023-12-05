package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.FieldDTOBuilder;
import tech.metavm.util.NncUtils;

import java.util.List;

public record MergeFieldDTO(
        String name,
        RefDTO fieldRef,
        RefDTO typeRef,
        List<ConditionalValueDTO> values
) implements FieldReferringDTO<MergeFieldDTO> {

    public MergeFieldDTO copyWithFieldRef(RefDTO fieldRef) {
        return new MergeFieldDTO(name, fieldRef, typeRef, values);
    }

    public FieldDTO toFieldDTO() {
        return FieldDTOBuilder.newBuilder(name, typeRef)
                .tmpId(NncUtils.get(fieldRef, RefDTO::tmpId))
                .id(NncUtils.get(fieldRef, RefDTO::id))
                .build();
    }

}
