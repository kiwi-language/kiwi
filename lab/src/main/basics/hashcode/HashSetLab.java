package hashcode;

import org.metavm.api.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class HashSetLab {

    private final Set<Object> set = new HashSet<>();

    public boolean add(Object object) {
        return set.add(object);
    }

    public boolean contains(Object object) {
        return set.contains(object);
    }

}
