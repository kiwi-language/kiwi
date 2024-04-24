package tech.metavm.entity;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Klass;

import java.util.Map;

public record TypeParseResult(
        Klass type,
        Map<ModelIdentity, Instance> instanceMap) {

}
