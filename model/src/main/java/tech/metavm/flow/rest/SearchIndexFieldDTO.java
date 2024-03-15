package tech.metavm.flow.rest;

public record SearchIndexFieldDTO(
        String indexFieldId,
        int operator,
        ValueDTO value
) {
}
