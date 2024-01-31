package tech.metavm.object.instance;

import tech.metavm.object.instance.rest.FieldValue;

import java.util.Map;

public record Source(long id, long typeId, Map<Long, FieldValue> fields) {
}
