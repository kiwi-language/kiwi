package org.metavm.object.type;

import java.util.Collection;

public interface TypeDefRepository extends IndexedTypeDefProvider {

    void save(TypeDef typeDef);

    default void save(Collection<TypeDef> typeDefs) {
       typeDefs.forEach(this::save);
    }

}
