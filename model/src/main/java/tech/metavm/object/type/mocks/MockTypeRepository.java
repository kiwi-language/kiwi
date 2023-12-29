package tech.metavm.object.type.mocks;

import org.jetbrains.annotations.Nullable;
import tech.metavm.common.RefDTO;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeRepository;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.Map;

public class MockTypeRepository implements TypeRepository {

    private final Map<RefDTO, Type> map = new HashMap<>();

    @Nullable
    @Override
    public ClassType findClassTypeByName(String name) {
        return (ClassType) NncUtils.find(
                map.values(),
                t -> t instanceof ClassType c && c.getName().equals(name)
        );
    }

    @Override
    public Type getType(RefDTO ref) {
        return map.get(ref);
    }

    @Override
    public void save(Type type) {
        map.put(type.getRef(), type);
    }
}
