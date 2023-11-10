package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;
import tech.metavm.object.meta.Access;
import tech.metavm.object.meta.rest.dto.FieldDTO;
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
        return new FieldDTO(
                NncUtils.get(fieldRef, RefDTO::tmpId),
                NncUtils.get(fieldRef, RefDTO::id),
                name,
                null,
                Access.PUBLIC.code(),
                null,
                false,
                false,
                null,
                typeRef,
                false,
                false
        );
    }

}
