package tech.metavm.object.meta;

import tech.metavm.flow.rest.ValueDTO;

import java.util.List;

public record IndexParam(
        List<IndexFieldDTO> fields
) {

    public static IndexParam create(String name, ValueDTO value) {
        return new IndexParam(
                List.of(new IndexFieldDTO(null, name, null, value))
        );
    }

    public int getKind() {
        return ConstraintKind.UNIQUE.code();
    }

}
