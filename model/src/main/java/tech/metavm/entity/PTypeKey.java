package tech.metavm.entity;

import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Type;

import java.util.List;

public record PTypeKey(Klass template, List<Type> typeArguments) {
}
