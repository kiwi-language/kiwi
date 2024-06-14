package org.metavm.flow;

import org.metavm.object.type.Type;

import java.util.List;

public record SimpleMethodRef(String name, List<Type> typeArguments) {
}
