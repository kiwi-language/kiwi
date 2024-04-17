package tech.metavm.entity;

import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;

import java.util.List;

public record PTypeKey(ClassType template, List<Type> typeArguments) {
}
