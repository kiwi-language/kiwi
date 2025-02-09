package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.StaticFieldTable;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.TriConsumer;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.metavm.entity.ReflectDefiner.ignoredFunctionalInterfaceMethods;

@Slf4j
public abstract class AbstractIdGenerator {

    protected final Class<?> clazz;
    private final BiConsumer<ModelIdentity, Id> idCollector;
    private final TriConsumer<ModelIdentity, Long, Long> rootCollector;
    private final Consumer<Class<?>> callback;
    private final long treeId;
    private long nextNodeId = 0;

    public AbstractIdGenerator(Class<?> clazz, long treeId,
                              BiConsumer<ModelIdentity, Id> idCollector,
                              TriConsumer<ModelIdentity, Long, Long> rootCollector,
                              Consumer<Class<?>> callback) {
        this.clazz = clazz;
        this.rootCollector = rootCollector;
        this.idCollector = idCollector;
        this.callback = callback;
        this.treeId = treeId;
    }

    public void generate() {
        if (clazz != Entity.class)
            generate(clazz);
        rootCollector.accept(ModelIdentities.getIdentity(clazz), treeId, nextNodeId);
    }

    private void generate(Class<?> javaClass) {
        initId(javaClass);
        var s = javaClass.getGenericSuperclass();
        if (s != null) processType(s);
        processTypes(javaClass.getGenericInterfaces());
        initIds(javaClass.getTypeParameters());
        for (Constructor<?> constructor : javaClass.getDeclaredConstructors()) {
            if (constructor.isSynthetic() || !isConstructorIncluded(constructor))
                continue;
            processExecutable(constructor);
        }
        var isFunctionalInterface = javaClass.isAnnotationPresent(FunctionalInterface.class);
        for (Method javaMethod : javaClass.getDeclaredMethods()) {
            if (isFunctionalInterface) {
                if (javaMethod.isDefault() || ignoredFunctionalInterfaceMethods.contains(ReflectDefiner.JavaMethodSignature.of(javaMethod)))
                    continue;
            }
            if (javaMethod.isSynthetic() || !isMethodIncluded(javaMethod))
                continue;
            processExecutable(javaMethod);
            processType(javaMethod.getGenericReturnType());
        }
        boolean sftRequired = false;
        for (Field field : javaClass.getDeclaredFields()) {
            if (isFieldIncluded(field)) {
                initId(field);
                processType(field.getGenericType());
                if (Modifier.isStatic(field.getModifiers())) {
                    var initialValue = ReflectionUtils.get(null, field);
                    if (initialValue instanceof Integer) sftRequired = true;
                }
            }
        }
        for (Class<?> innerClass : javaClass.getDeclaredClasses()) {
            if (Modifier.isPublic(innerClass.getModifiers())) {
                generate(innerClass);
            }
        }
        if (sftRequired)
            initId(ModelIdentity.create(StaticFieldTable.class, javaClass.getName().replace('$', '.')));
    }


    protected abstract boolean isFieldIncluded(Field field);

    protected  abstract boolean isConstructorIncluded(Constructor<?> constructor);

    protected abstract boolean isMethodIncluded(Method method);

    private boolean processExecutable(Executable javaMethod) {
        initId(javaMethod);
        initIds(javaMethod.getTypeParameters());
        initIds(javaMethod.getParameters());
        for (var typeParameter : javaMethod.getTypeParameters()) {
            processTypes(typeParameter.getBounds());
        }
        for (Parameter parameter : javaMethod.getParameters()) {
            processType(parameter.getParameterizedType());
        }
        return true;
    }

    protected void initIds(Object[] a) {
        for (Object o : a) {
            initId(o);
        }
    }

    protected void initId(Object o) {
        var identity = o instanceof ModelIdentity i ? i : ModelIdentities.getIdentity(o);
        idCollector.accept(identity, PhysicalId.of(treeId, nextNodeId++));
    }

    private void processTypes(Type[] types) {
        for (Type type : types) {
            processType(type);
        }
    }

    protected void processType(Type type) {
        switch (type) {
            case Class<?> c -> {
                if (c.isPrimitive() || c == Object.class || c == Value.class)
                    return;
                if (c.isArray()) processType(c.getComponentType());
                else callback.accept(c);
            }
            case ParameterizedType pType -> {
                processType(pType.getRawType());
                for (Type actualTypeArgument : pType.getActualTypeArguments()) {
                    processType(actualTypeArgument);
                }
            }
            case GenericArrayType arrayType -> processType(arrayType.getGenericComponentType());
            case TypeVariable<?> tv -> {
                var decl = tv.getGenericDeclaration();
                switch (decl) {
                    case Class<?> c -> processType(c);
                    case Method m -> processType(m.getDeclaringClass());
                    default -> throw new IllegalArgumentException("Unrecognized generation declaration: " + decl);
                }
            }
            case WildcardType wildcardType -> {
                for (Type lowerBound : wildcardType.getLowerBounds()) {
                    processType(lowerBound);
                }
                for (Type upperBound : wildcardType.getUpperBounds()) {
                    processType(upperBound);
                }
            }
            default -> throw new IllegalArgumentException("Unrecognized java type: " + type);
        }
    }


}
