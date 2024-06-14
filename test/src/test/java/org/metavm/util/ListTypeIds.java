package org.metavm.util;

public record ListTypeIds(
        String listTypeId,
        String listValueTypeId,
        String listLabelFieldId,
        String listValueFieldId,
        NodeTypeIds nodeTypeIds
) {
}
