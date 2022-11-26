package tech.metavm.object.meta;

import tech.metavm.flow.rest.ValueDTO;

import java.util.List;

public record UniqueConstraintParam(
        List<UniqueConstraintItemDTO> items
) {

    public static UniqueConstraintParam create(String name, ValueDTO value) {
        return new UniqueConstraintParam(
                List.of(
                        new UniqueConstraintItemDTO(
                                name, value
                        )
                )
        );
    }

}
