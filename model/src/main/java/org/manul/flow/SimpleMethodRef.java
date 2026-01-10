package org.manul.flow;

import org.manul.object.type.Type;

import java.util.List;

public record SimpleMethodRef(String name, List<Type> typeArguments) {
}
