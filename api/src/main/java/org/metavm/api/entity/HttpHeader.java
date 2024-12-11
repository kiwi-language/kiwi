package org.metavm.api.entity;

import org.metavm.api.ValueObject;
import org.metavm.api.Value;

@Value
public record HttpHeader(String name, String value) implements ValueObject {

}
