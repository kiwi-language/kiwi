package tech.metavm.entity;

import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class WrappedIdProvider implements EntityIdProvider {

//    private final DefContext defContext;

    private final EntityIdProvider underlyingIdProvider;

    private final Function<Long, Long> getTypeIdInterceptor;

//    public static final Map<Long, java.lang.reflect.Type> ID_TO_JAVA_TYPE = Map.of(
//        -1L, TenantRT.class
//    );

    public WrappedIdProvider(Function<Long, Long> getTypeIdInterceptor, EntityIdProvider underlyingIdProvider) {
        this.getTypeIdInterceptor = getTypeIdInterceptor;
        this.underlyingIdProvider = underlyingIdProvider;
    }


    @Override
    public long getTypeId(long id) {
        Long typeId = getTypeIdInterceptor.apply(id);
        if(typeId != null) {
            return typeId;
        }
        return underlyingIdProvider.getTypeId(id);
//        java.lang.reflect.Type javaType = ID_TO_JAVA_TYPE.get(id);
//        if(javaType != null) {
//            return defContext.getType(javaType).getId();
//        }
//        else {
//        return underlyingIdProvider.getTypeId(id);
//        }
    }

    @Override
    public Map<Type, List<Long>> allocate(long tenantId, Map<Type, Integer> typeId2count) {
        return underlyingIdProvider.allocate(tenantId, typeId2count);
    }
}
