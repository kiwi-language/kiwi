package org.metavm.object.type.mocks;

import org.jetbrains.annotations.Nullable;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeDef;
import org.metavm.object.type.TypeDefRepository;
import org.metavm.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class MockTypeDefRepository implements TypeDefRepository {

    private final Map<Id, TypeDef> map = new HashMap<>();

    @Nullable
    @Override
    public Klass findKlassByName(String name) {
        return (Klass) Utils.find(
                map.values(),
                t -> t instanceof Klass c && c.getName().equals(name)
        );
    }

    @Override
    public TypeDef getTypeDef(Id id) {
        return map.get(id);
    }

    @Override
    public void save(TypeDef typeDef) {
        map.put(typeDef.getId(), typeDef);
    }
}
