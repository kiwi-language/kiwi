package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

public class RecordDef<T extends Record> extends PojoDef<T> {

    private final Constructor<T> constructor;

    public RecordDef(String name, Class<T> entityType, @Nullable PojoDef<? super T> parentDef, Type type) {
        super(name, entityType, parentDef, type);
        Class<?>[] componentTypes =
                Arrays.stream(entityType.getRecordComponents()).map(RecordComponent::getType).toArray(Class<?>[]::new);
        constructor = ReflectUtils.getDeclaredConstructor(entityType, componentTypes);
    }

    @Override
    public T newModel(Instance instance, ModelMap modelMap) {
        Object[] fieldValues = NncUtils.map(getFieldDefs(), fieldDef -> fieldDef.getFieldValue(instance, modelMap))
                .toArray(Object[]::new);
        return ReflectUtils.invokeConstructor(constructor, fieldValues);
    }

    @Override
    public void updateModel(T pojo, Instance instance, ModelMap modelMap) {

    }

}
