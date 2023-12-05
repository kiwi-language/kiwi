package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

import java.util.List;

public record FunctionTypeKey(List<RefDTO> parameterTypeRefs, RefDTO returnTypeRef) implements TypeKey {
}
