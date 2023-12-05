package tech.metavm.object.type.rest.dto;

import tech.metavm.common.RefDTO;

public record TypeVariableKey(RefDTO genericDeclarationRef, int index) implements TypeKey {
}
