package tech.metavm.object.type;

import java.util.Collection;

public interface TypeRepository extends IndexedTypeProvider {

    void save(Type type);

    default void save(Collection<Type> types) {
        types.forEach(this::save);
    }

}
