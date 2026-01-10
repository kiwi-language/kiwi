package org.manul.util;

public record ListTypeIds(
        String listTypeId,
        String listValueTypeId,
        String listLabelFieldId,
        String listValueFieldId,
        NodeTypeIds nodeTypeIds
) {
}
