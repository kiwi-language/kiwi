package tech.metavm.object.type.mocks;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

public class MockTypeDefRepository implements TypeDefRepository {

    private final Map<Id, TypeDef> map = new HashMap<>();

    @Nullable
    @Override
    public Klass findKlassByName(String name) {
        return (Klass) NncUtils.find(
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
