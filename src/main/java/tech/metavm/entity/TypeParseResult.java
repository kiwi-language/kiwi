package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;

import java.util.Map;

public record TypeParseResult(
        ClassType type,
        Map<ModelIdentity, Instance> instanceMap) {

}
