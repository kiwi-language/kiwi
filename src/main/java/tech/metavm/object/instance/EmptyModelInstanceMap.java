package tech.metavm.object.instance;

import tech.metavm.entity.ModelDef;

import javax.annotation.Nullable;

public class EmptyModelInstanceMap implements ModelInstanceMap{
    @Override
    public Instance getInstance(Object model) {
        return null;
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance) {
        return null;
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance, @Nullable ModelDef<?, ?> def) {
        return null;
    }
}
