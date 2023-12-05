package tech.metavm.flow.rest;

import tech.metavm.common.RefDTO;

public record SearchIndexFieldDTO(
        RefDTO indexFieldRef, int operator,
        ValueDTO value
) {
}
