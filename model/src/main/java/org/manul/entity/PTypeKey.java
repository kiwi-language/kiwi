package org.manul.entity;

import org.manul.object.type.Klass;
import org.manul.object.type.Type;

import java.util.List;

public record PTypeKey(Klass template, List<Type> typeArguments) {
}
