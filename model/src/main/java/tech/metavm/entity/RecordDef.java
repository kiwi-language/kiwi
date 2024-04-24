package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.type.Klass;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.Arrays;

public class RecordDef<T extends Record> extends PojoDef<T> {

    private final Constructor<T> constructor;

    public RecordDef(Class<T> javaType, Type genericType, @Nullable PojoDef<? super T> parentDef, Klass type, DefContext defContext) {
        super(javaType, genericType, parentDef, type, defContext);
        Class<?>[] componentTypes =
                Arrays.stream(javaType.getRecordComponents()).map(RecordComponent::getType).toArray(Class<?>[]::new);
        constructor = ReflectionUtils.getDeclaredConstructor(javaType, componentTypes);
    }

    @Override
    public void initModel(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T createModel(ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        Object[] fieldValues = NncUtils.map(getFieldDefList(), fieldDef -> fieldDef.getModelFieldValue(instance, objectInstanceMap))
                .toArray(Object[]::new);
        return ReflectionUtils.invokeConstructor(constructor, fieldValues);
    }

    @Override
    public void updateModel(T pojo, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {

    }

    @Override
    public boolean isProxySupported() {
        return false;
    }
}
