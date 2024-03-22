package tech.metavm.object.instance;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.TypeCategory;

import java.util.Map;

public record Source(Id id, Map<Id, FieldValue> fields) {

}
