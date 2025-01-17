package org.metavm.object.instance.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayInstanceWrap extends InstanceWrap {
    private final List<Object> list;

    public ArrayInstanceWrap(List<Object> list) {
        this.list = new ArrayList<>(list);
    }

    public Object get(int i) {
        return convertValue(list.get(i));
    }

    public ClassInstanceWrap getObject(int i) {
        return (ClassInstanceWrap) get(i);
    }

    public ArrayInstanceWrap getArray(int i) {
        return (ArrayInstanceWrap) get(i);
    }

    public List<Object> toList() {
        return Collections.unmodifiableList(list);
    }

    public int size() {
        return list.size();
    }

    @Override
    public String toString() {
        return list.toString();
    }

    public Object getFirst() {
        return get(0);
    }
}
