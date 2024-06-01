package tech.metavm.object.instance;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.rest.dto.TypeKey;

import java.util.Map;

public record Source(Id id, TypeKey typeKey, Map<Id, FieldValue> fields) {

}
