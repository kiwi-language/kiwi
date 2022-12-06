package tech.metavm.object.instance;

public class EmptyModelInstanceMap implements ModelInstanceMap{
    @Override
    public Instance getInstance(Object model) {
        return null;
    }

    @Override
    public <T> T getModel(Class<T> klass, Instance instance) {
        return null;
    }
}
