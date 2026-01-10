package org.manul.object.instance;

import lombok.extern.slf4j.Slf4j;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Value;
import org.manul.object.type.rest.dto.TypeKey;

import java.util.Map;

@Slf4j
public record Source(Id id, TypeKey typeKey, Map<String, Value> fields) {

}
