package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;

public class RecordDef<T extends Record> extends PojoDef<T> {

    private final Constructor<T> constructor;

    public RecordDef(Class<T> javaType, Type genericType, @Nullable PojoDef<? super T> parentDef, ClassType type, DefContext defContext) {
        super(javaType, genericType, parentDef, type, defContext);
        Class<?>[] componentTypes =
                Arrays.stream(javaType.getRecordComponents()).map(RecordComponent::getType).toArray(Class<?>[]::new);
        constructor = ReflectUtils.getDeclaredConstructor(javaType, componentTypes);
    }

    @Override
    public void initModel(T model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T createModel(ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        Object[] fieldValues = NncUtils.map(getFieldDefList(), fieldDef -> fieldDef.getModelFieldValue(instance, modelInstanceMap))
                .toArray(Object[]::new);
        return ReflectUtils.invokeConstructor(constructor, fieldValues);
    }

    @Override
    public void updateModel(T pojo, ClassInstance instance, ModelInstanceMap modelInstanceMap) {

    }

    @Override
    public boolean isProxySupported() {
        return false;
    }
}
