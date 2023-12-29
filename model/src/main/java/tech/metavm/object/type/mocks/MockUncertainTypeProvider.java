package tech.metavm.object.type.mocks;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.UncertainType;
import tech.metavm.object.type.UncertainTypeProvider;

import java.util.HashMap;
import java.util.Map;

public class MockUncertainTypeProvider implements UncertainTypeProvider {

    private final Map<Key, UncertainType> map = new HashMap<>();

    @Override
    public UncertainType getUncertainType(Type lowerBound, Type upperBound, @Nullable Long tmpId) {
        return map.computeIfAbsent(new Key(lowerBound, upperBound),
                k -> new UncertainType(tmpId, lowerBound, upperBound));
    }

    private record Key(Type lowerBound, Type upperBound) {
    }

}
