package tech.metavm.flow;

import tech.metavm.object.type.Type;

import java.util.List;

public record SimpleMethodRef(String name, List<Type> typeArguments) {
}
