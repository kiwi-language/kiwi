package tech.metavm.object.meta;

import tech.metavm.flow.rest.ValueDTO;

import java.util.List;

public record UniqueConstraintParamDTO(
        List<UniqueConstraintItemDTO> items
) {

    public static UniqueConstraintParamDTO create(String name, ValueDTO value) {
        return new UniqueConstraintParamDTO(
                List.of(
                        new UniqueConstraintItemDTO(
                                null, name, value
                        )
                )
        );
    }

    public int getKind() {
        return ConstraintKind.UNIQUE.code();
    }

}
