package org.metavm.api;

import java.util.ArrayList;
import java.util.Collection;

public class ReadonlyList<T> extends ArrayList<T> {

    public ReadonlyList() {
    }

    public ReadonlyList(Collection<? extends T> c) {
        super(c);
    }
}
