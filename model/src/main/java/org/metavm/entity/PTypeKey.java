package org.metavm.entity;

import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;

import java.util.List;

public record PTypeKey(Klass template, List<Type> typeArguments) {
}
