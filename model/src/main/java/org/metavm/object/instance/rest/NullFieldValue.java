package org.metavm.object.instance.rest;

import java.util.Set;

public class NullFieldValue extends FieldValue {

    public static final NullFieldValue instance = new NullFieldValue();

    public NullFieldValue() {
        super("null");
    }

    @Override
    public int getKind() {
        return 9;
    }

    @Override
    public boolean valueEquals(FieldValue that, Set<String> newIds) {
        return that instanceof NullFieldValue;
    }

    @Override
    public Object toJson() {
        return null;
    }
}
