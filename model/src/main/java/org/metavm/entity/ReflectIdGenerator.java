package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.util.TriConsumer;

import java.lang.reflect.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ReflectIdGenerator extends AbstractIdGenerator {

    public ReflectIdGenerator(Class<?> clazz, long treeId,
                              BiConsumer<ModelIdentity, Id> idCollector,
                              TriConsumer<ModelIdentity, Long, Long> rootCollector,
                              Consumer<Class<?>> callback) {
        super(clazz, treeId, idCollector, rootCollector, callback);
    }

    @Override
    protected boolean isConstructorIncluded(Constructor<?> constructor) {
        var mods = constructor.getModifiers();
        return Modifier.isPublic(mods) || Modifier.isProtected(mods);
    }

    @Override
    protected boolean isMethodIncluded(Method method) {
        var mods = method.getModifiers();
        return Modifier.isPublic(mods) || Modifier.isProtected(mods);
    }

    @Override
    protected void processType(Type type) {
        if (type != Class.class) super.processType(type);
    }
}
