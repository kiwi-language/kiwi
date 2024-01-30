package tech.metavm.util;

public record ListTypeIds(
        long listTypeId,
        long listValueTypeId,
        long listLabelFieldId,
        long listValueFieldId,
        NodeTypeIds nodeTypeIds
) {
}
