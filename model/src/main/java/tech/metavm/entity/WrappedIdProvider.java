package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class WrappedIdProvider implements EntityIdProvider {

    private final EntityIdProvider underlyingIdProvider;

    private final Function<Id, TypeId> getTypeIdInterceptor;

    public WrappedIdProvider(Function<Id, TypeId> getTypeIdInterceptor, EntityIdProvider underlyingIdProvider) {
        this.getTypeIdInterceptor = getTypeIdInterceptor;
        this.underlyingIdProvider = underlyingIdProvider;
    }

    @Override
    public TypeId getTypeId(Id id) {
        var typeId = getTypeIdInterceptor.apply(id);
        if(typeId != null) {
            return typeId;
        }
        return underlyingIdProvider.getTypeId(id);
    }

    @Override
    public Map<Type, List<Long>> allocate(long appId, Map<Type, Integer> typeId2count) {
        return underlyingIdProvider.allocate(appId, typeId2count);
    }
}
