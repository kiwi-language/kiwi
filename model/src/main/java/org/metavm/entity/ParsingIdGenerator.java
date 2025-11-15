package org.metavm.entity;

import org.metavm.api.EntityFlow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.util.TriConsumer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ParsingIdGenerator extends AbstractIdGenerator {

    public ParsingIdGenerator(Class<?> clazz, long treeId, BiConsumer<ModelIdentity, Id> idCollector, TriConsumer<ModelIdentity, Long, Long> rootCollector, Consumer<Class<?>> callback) {
        super(clazz, treeId, idCollector, rootCollector, callback);
    }

    @Override
    public void generate() {
        super.generate();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == IndexDef.class) {
                initId(field);
            }
        }
    }

    @Override
    protected boolean isConstructorIncluded(Constructor<?> constructor) {
        return constructor.isAnnotationPresent(EntityFlow.class);
    }

    @Override
    protected boolean isMethodIncluded(Method method) {
        return method.isAnnotationPresent(EntityFlow.class);
    }

    @Override
    protected void processType(Type type) {
        if (type == Value.class)
            return;
        if (type == Class.class)
            type = Klass.class;
        super.processType(type);
    }
}
