package org.metavm.object.instance.rest;

import java.util.Set;

public class StringFieldValue extends FieldValue {

    private final String value;

    public StringFieldValue(String value) {
        super(value);
        this.value = value;
    }

    @Override
    public int getKind() {
        return 10;
    }

    @Override
    public boolean valueEquals(FieldValue that, Set<String> newIds) {
        return that instanceof StringFieldValue f && value.equals(f.value);
    }

    @Override
    public Object toJson() {
        return value;
    }

    public String getValue() {
        return value;
    }
}
