package org.metavm.object.instance;

import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.type.rest.dto.TypeKey;

import java.util.Map;

public record Source(Id id, TypeKey typeKey, Map<Id, FieldValue> fields) {

}
