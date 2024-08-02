package org.metavm.entity;

import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;

import java.util.Map;

public record TypeParseResult(
        Klass type,
        Map<ModelIdentity, Value> instanceMap) {

}
