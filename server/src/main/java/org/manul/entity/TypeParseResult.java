package org.manul.entity;

import org.manul.object.instance.core.Value;
import org.manul.object.type.Klass;

import java.util.Map;

public record TypeParseResult(
        Klass type,
        Map<ModelIdentity, Value> instanceMap) {

}
