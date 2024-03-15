package tech.metavm.entity;

import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TypeId;

import java.util.Collection;
import java.util.function.Function;

public class WrappedIdInitializer implements IdInitializer{

    private final IdInitializer underlyingIdInitializer;

    private final Function<Id, TypeId> getTypeIdInterceptor;

    public WrappedIdInitializer(Function<Id, TypeId> getTypeIdInterceptor, IdInitializer underlyingIdInitializer) {
        this.getTypeIdInterceptor = getTypeIdInterceptor;
        this.underlyingIdInitializer = underlyingIdInitializer;
    }

    @Override
    public TypeId getTypeId(Id id) {
        var typeId = getTypeIdInterceptor.apply(id);
        if(typeId != null) {
            return typeId;
        }
        return underlyingIdInitializer.getTypeId(id);
    }

    @Override
    public void initializeIds(Id appId, Collection<? extends DurableInstance> instances) {
        underlyingIdInitializer.initializeIds(appId, instances);
    }
}
