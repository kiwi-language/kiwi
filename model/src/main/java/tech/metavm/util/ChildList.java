package tech.metavm.util;

import java.util.ArrayList;
import java.util.List;

public class ChildList<E> {

    private final List<E> list = new ArrayList<>();

    public void add(E e) {
        list.add(e);
    }

    public void remove(E e) {
        list.remove(e);
    }

}
