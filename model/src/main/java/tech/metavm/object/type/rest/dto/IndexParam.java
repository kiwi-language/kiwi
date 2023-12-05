package tech.metavm.object.type.rest.dto;

import tech.metavm.flow.rest.ValueDTO;

import java.util.List;

public record IndexParam(
        boolean unique,
        List<IndexFieldDTO> fields
) implements ConstraintParam {

    public static IndexParam create(boolean unique, String name, ValueDTO value) {
        return new IndexParam(
                unique, List.of(new IndexFieldDTO(null, name, null, value))
        );
    }

    public int getKind() {
        return ConstraintKindCodes.UNIQUE;
    }

}
