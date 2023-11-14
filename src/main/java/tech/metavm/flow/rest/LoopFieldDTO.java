package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.object.meta.rest.dto.FieldDTOBuilder;
import tech.metavm.util.NncUtils;

public record LoopFieldDTO(
        RefDTO fieldRef,
        String name,
        RefDTO typeRef,
        ValueDTO initialValue,
        ValueDTO updatedValue
) implements FieldReferringDTO<LoopFieldDTO> {

    public LoopFieldDTO copyWithFieldRef(RefDTO fieldRef) {
        return new LoopFieldDTO(fieldRef, name, typeRef, initialValue, updatedValue);
    }

    public FieldDTO toFieldDTO() {
        return FieldDTOBuilder.newBuilder(name, null, typeRef)
                .tmpId(NncUtils.get(fieldRef, RefDTO::tmpId))
                .id(NncUtils.get(fieldRef, RefDTO::id))
                .build();
    }

}
