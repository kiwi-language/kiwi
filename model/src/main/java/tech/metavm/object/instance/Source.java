package tech.metavm.object.instance;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.TypeCategory;

import java.util.Map;

public record Source(long id, TypeTag typeTag, long typeId, Map<Id, FieldValue> fields) {

    public Id getId() {
        return PhysicalId.of(id, typeTag, typeId);
    }

}
