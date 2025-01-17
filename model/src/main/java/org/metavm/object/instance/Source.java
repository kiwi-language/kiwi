package org.metavm.object.instance;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.rest.dto.TypeKey;

import java.util.Map;

@Slf4j
public record Source(Id id, TypeKey typeKey, Map<String, Value> fields) {

}
