package tech.metavm.object.type.mocks;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.IntersectionType;
import tech.metavm.object.type.IntersectionTypeProvider;
import tech.metavm.object.type.Type;

import java.util.*;

public class MockIntersectionTypeProvider implements IntersectionTypeProvider {

    private final Map<Key, IntersectionType> map = new HashMap<>();

    @Override
    public IntersectionType getIntersectionType(Set<Type> types, @Nullable Long tmpId) {
        var typeSet = new HashSet<>(types);
        return map.computeIfAbsent(new Key(typeSet), k -> new IntersectionType(null, typeSet));
    }

    public void add(IntersectionType type) {
        map.put(new Key(type.getTypes()), type);
    }

    private record Key(Set<Type> types) {
    }
}
