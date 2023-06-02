package tech.metavm.spoon;

import java.util.Collection;
import java.util.Set;

public class SetLab {

    public Set<?> test() {
        return Set.of(1, 2, 3);
    }

    public void addAll(Set<Object> set, Collection<? extends String> all) {
        set.addAll(all);
    }

}
