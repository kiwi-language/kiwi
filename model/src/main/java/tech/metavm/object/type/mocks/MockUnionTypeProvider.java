package tech.metavm.object.type.mocks;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.UnionType;
import tech.metavm.object.type.UnionTypeProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockUnionTypeProvider implements UnionTypeProvider {

    private final Map<Key, UnionType> map = new HashMap<>();

    @Override
    public UnionType getUnionType(Set<Type> types, @Nullable Long tmpId) {
        return map.computeIfAbsent(new Key(types), k -> new UnionType(tmpId, types));
    }

    public void add(UnionType unionType) {
        map.put(new Key(unionType.getMembers()), unionType);
    }

    private record Key(Set<Type> types) {}

}
