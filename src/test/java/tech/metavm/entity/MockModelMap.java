package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MockModelMap implements ModelMap {

    private final Map<IInstance, Object> map = new HashMap<>();

    private Function<Instance, Object> loader;

    public void setLoader(Function<Instance, Object> loader) {
        this.loader = loader;
    }

    public MockModelMap() {
        loader = null;
    }

    public MockModelMap(Function<Instance, Object> loader) {
        this.loader = loader;
    }

    @Override
    public <T> T get(Class<T> klass, Instance instance) {
        Object existing;
        if((existing = map.get(instance)) != null) {
            return klass.cast(existing);
        }
        else {
            Object ref = createRef(instance);
            map.put(instance, ref);
            return klass.cast(ref);
        }
    }

    private Object createRef(Instance instance) {
        Class<?> entityType = ReflectUtils.getRawClass(instance.getEntityType());
        NncUtils.requireNonNull(entityType);
        if(Modifier.isFinal(entityType.getModifiers())) {
            return loader.apply(instance);
        }
        else {
            return createRefHelper(instance, entityType);
        }
    }

    private <T> T createRefHelper(Instance instance, Class<T> entityType) {
        return EntityProxyFactory.getProxyInstance(
                entityType,
                instance.getId(),
                () -> entityType.cast(loader.apply(instance))
        );
    }

}
