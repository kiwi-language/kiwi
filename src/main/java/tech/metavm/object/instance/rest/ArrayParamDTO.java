package tech.metavm.object.instance.rest;

import java.util.List;

public record ArrayParamDTO (
        List<FieldValueDTO> elements
) {
}
