package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

public class RecordDef<T extends Record> extends PojoDef<T> {

    private final Constructor<T> constructor;

    public RecordDef(String name, Class<T> entityType, @Nullable PojoDef<? super T> parentDef, Type type, DefMap defMap) {
        super(name, entityType, parentDef, type, defMap);
        Class<?>[] componentTypes =
                Arrays.stream(entityType.getRecordComponents()).map(RecordComponent::getType).toArray(Class<?>[]::new);
        constructor = ReflectUtils.getDeclaredConstructor(entityType, componentTypes);
    }

    @Override
    public void initModel(T model, Instance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T createModel(Instance instance, ModelInstanceMap modelInstanceMap) {
        Object[] fieldValues = NncUtils.map(getFieldDefList(), fieldDef -> fieldDef.getModelFieldValue(instance, modelInstanceMap))
                .toArray(Object[]::new);
        return ReflectUtils.invokeConstructor(constructor, fieldValues);
    }

    @Override
    public void updateModel(T pojo, Instance instance, ModelInstanceMap modelInstanceMap) {

    }

    @Override
    public boolean isProxySupported() {
        return false;
    }
}
