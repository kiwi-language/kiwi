package org.manul.object.type.mocks;

import org.jetbrains.annotations.Nullable;
import org.manul.object.instance.core.Id;
import org.manul.object.type.Klass;
import org.manul.object.type.TypeDef;
import org.manul.object.type.TypeDefRepository;
import org.manul.util.Utils;

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
