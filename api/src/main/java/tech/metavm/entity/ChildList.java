package tech.metavm.entity;

import java.util.ArrayList;
import java.util.Collection;

public class ChildList<T> extends ArrayList<T> {

    public ChildList() {
    }

    public ChildList(Collection<? extends T> c) {
        super(c);
    }
}
