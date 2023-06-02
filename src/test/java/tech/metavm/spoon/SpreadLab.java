package tech.metavm.spoon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class SpreadLab {

    public <E> List<E> newList(Collection<E> collection) {
        return new ArrayList<>(new HashSet<>(collection));
    }

    public <E> void addAll(List<E> list, List<E> that) {
        list.addAll(that);
    }

}
