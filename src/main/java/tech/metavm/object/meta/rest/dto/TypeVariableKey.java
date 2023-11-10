package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

public record TypeVariableKey(RefDTO genericDeclarationRef, int index) implements TypeKey {
}
