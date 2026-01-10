package org.manul.object.type.rest.dto;

import java.util.List;

public record PreUpgradeRequest(
        List<FieldAdditionDTO> fieldAdditions,
        String initializers,
        List<String> newKlassIds,
        String walContent
) {
}
