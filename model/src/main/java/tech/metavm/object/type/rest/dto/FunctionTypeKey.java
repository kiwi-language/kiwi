package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

import java.util.List;

public record FunctionTypeKey(List<String> parameterTypeIds, String returnTypeId) implements TypeKey {
}
