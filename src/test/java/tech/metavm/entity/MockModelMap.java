package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
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
    public <T> T get(Class<T> klass, IInstance instance) {
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

    private Object createRef(IInstance instance) {
        Class<?> entityType = instance.getEntityType();
        NncUtils.requireNonNull(entityType);
        if(entityType == Table.class) {
            return new Table<>(() -> loadArrayElements(instance));
        }
        else if(Modifier.isFinal(entityType.getModifiers())) {
            return loader.apply(EntityContext.getRealInstance(instance));
        }
        else {
            return EntityProxyFactory.getProxyInstance(
                    entityType,
                    instance,
                    loader
            );
        }
    }

    private List<Object> loadArrayElements(IInstance instance) {
        InstanceArray instanceArray = (InstanceArray) EntityContext.getRealInstance(instance);
        return NncUtils.map(
                instanceArray.getElements(),
                this::createRef
        );
    }

}
