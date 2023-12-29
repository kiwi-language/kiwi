package tech.metavm.object.type.mocks;

import tech.metavm.object.type.FunctionType;
import tech.metavm.object.type.FunctionTypeProvider;
import tech.metavm.object.type.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockFunctionTypeProvider implements FunctionTypeProvider {

    private final Map<Key, FunctionType> map = new HashMap<>();

    @Override
    public FunctionType getFunctionType(List<Type> parameterTypes, Type returnType, Long tmpId) {
        return map.computeIfAbsent(new Key(parameterTypes, returnType),
                k -> new FunctionType(tmpId, parameterTypes, returnType));
    }

    private record Key(List<Type> parameterTypes, Type returnType) {}

}
