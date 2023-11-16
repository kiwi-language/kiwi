package tech.metavm.object.instance;

import tech.metavm.entity.DefContext;
import tech.metavm.entity.ModelDef;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PrimitiveInstance;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;

public class PrimitiveInstanceMap implements ModelInstanceMap{

    private final DefContext defContext;

    public PrimitiveInstanceMap(DefContext defContext) {
        this.defContext = defContext;
    }

    @Override
    public Instance getInstance(Object model) {
        return InstanceUtils.serializeEntityPrimitive(model, defContext::getType);
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance) {
        return getModel(klass, instance, null);
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance, @Nullable ModelDef<?, ?> def) {
        if(instance instanceof PrimitiveInstance primitiveInstance)
            return klass.cast(InstanceUtils.deserializeEntityPrimitive(primitiveInstance, klass));
        else
            throw new InternalException(String.format("Can not get model for instance %s", instance));
    }
}
