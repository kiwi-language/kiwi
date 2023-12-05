package tech.metavm.entity;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;

import java.util.Map;

public record TypeParseResult(
        ClassType type,
        Map<ModelIdentity, Instance> instanceMap) {

}
