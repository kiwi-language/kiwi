package tech.metavm.entity;

import tech.metavm.object.instance.core.DurableInstance;

import java.util.Collection;
import java.util.function.Function;

public class WrappedIdInitializer implements IdInitializer{

    private final IdInitializer underlyingIdInitializer;

    private final Function<Long, Long> getTypeIdInterceptor;

    public WrappedIdInitializer(Function<Long, Long> getTypeIdInterceptor, IdInitializer underlyingIdInitializer) {
        this.getTypeIdInterceptor = getTypeIdInterceptor;
        this.underlyingIdInitializer = underlyingIdInitializer;
    }

    @Override
    public long getTypeId(long id) {
        Long typeId = getTypeIdInterceptor.apply(id);
        if(typeId != null) {
            return typeId;
        }
        return underlyingIdInitializer.getTypeId(id);
    }

    @Override
    public void initializeIds(long appId, Collection<? extends DurableInstance> instances) {
        underlyingIdInitializer.initializeIds(appId, instances);
    }
}
