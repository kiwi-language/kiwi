package org.metavm.object.type.rest.dto;

import org.metavm.flow.rest.ValueDTO;
import org.metavm.object.type.ConstraintKind;

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
        return ConstraintKind.UNIQUE.code();
    }

}
