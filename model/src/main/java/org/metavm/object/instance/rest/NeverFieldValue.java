package org.metavm.object.instance.rest;

import java.util.Set;

public class NeverFieldValue extends FieldValue {
    public NeverFieldValue() {
        super("never");
    }

    @Override
    public int getKind() {
        return 0;
    }

    @Override
    public boolean valueEquals(FieldValue that, Set<String> newIds) {
        return false;
    }

    @Override
    public Object toJson() {
        return null;
    }
}
