package org.metavm.entity;

import javassist.util.proxy.ProxyObject;
import lombok.extern.slf4j.Slf4j;
import org.metavm.api.*;
import org.metavm.flow.Function;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.NativeEphemeralObject;
import org.metavm.object.type.EnumConstantRT;
import org.metavm.object.type.Klass;
import org.metavm.util.LinkedList;
import org.metavm.util.Reference;
import org.metavm.util.*;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.metavm.util.ReflectionUtils.*;

@Slf4j
public class EntityUtils {

    public static final long MAXIMUM_DIFF_DEPTH = 1000;

    public static final Set<Class<?>> PRIM_CLASSES = Set.of(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, String.class, Date.class, LocalDateTime.class,
            BigInteger.class, BigDecimal.class, Class.class, Object.class
    );

    public static final Set<Class<?>> ENTITY_CLASSES = Set.of(
            Klass.class, org.metavm.object.type.Field.class, org.metavm.object.instance.core.Value.class,
            EnumConstantRT.class
    );


    public static String getMetaFieldName(Field javaField) {
        if (ENUM_NAME_FIELD.equals(javaField)) {
            return "name";
        }
        if (ENUM_ORDINAL_FIELD.equals(javaField)) {
            return "ordinal";
        }
        var entityField = javaField.getAnnotation(EntityField.class);
        return Utils.safeCall(entityField, EntityField::value);
    }

    public static String getMetaConstraintName(Field field) {
        var anno = field.getAnnotation(EntityConstraint.class);
        return anno != null && !anno.value().isEmpty() ? anno.value() : field.getName();
    }

    public static String getMetaFlowName(Method method) {
        var anno = method.getAnnotation(EntityFlow.class);
        return anno != null && !anno.value().isEmpty() ? anno.value() : method.getName();
    }

    public static String getMetaTypeName(Class<?> javaType) {
        var entityType = javaType.getAnnotation(org.metavm.api.Entity.class);
        var valueType = javaType.getAnnotation(Value.class);
        var entityStruct = javaType.getAnnotation(EntityStruct.class);
        var valueStruct = javaType.getAnnotation(ValueStruct.class);
        return Utils.firstNonBlank(
                Utils.safeCall(entityType, org.metavm.api.Entity::value),
                Utils.safeCall(valueType, Value::value),
                Utils.safeCall(entityStruct, EntityStruct::value),
                Utils.safeCall(valueStruct, ValueStruct::value),
                javaType.getSimpleName()
        );
    }

    private static boolean isCompiled(Class<?> klass) {
        var entityType = klass.getAnnotation(org.metavm.api.Entity.class);
        if (entityType != null && entityType.compiled()) {
            return true;
        }
        var valueType = klass.getAnnotation(Value.class);
        return valueType != null && valueType.compiled();
    }

    public static String getMetaTypeVariableName(TypeVariable<?> typeVariable) {
        var anno = typeVariable.getAnnotation(TemplateVariable.class);
        return anno != null ? anno.value() : typeVariable.getName();
    }

    public static Method tryGetMethodByName(Class<?> klass, String methodName) {
        return getMethodByName(klass, methodName, false);
    }


    public static Set<ModelAndPath> getReachableObjects(Collection<Object> objects,
                                                        Predicate<Object> filter,
                                                        boolean ignoreTransientFields) {
        Set<ModelAndPath> result = new IdentitySet<>();
        Set<Object> visited = new IdentitySet<>();
        for (Object object : objects) {
            getReachableObjects0(object, filter, ignoreTransientFields, new LinkedList<>(), visited, result);
        }
        return result;
    }

    private static void getReachableObjects0(Object object,
                                             Predicate<Object> filter,
                                             boolean ignoreTransientFields,
                                             LinkedList<String> path,
                                             Set<Object> visited,
                                             Set<ModelAndPath> result) {
        if (object == null || ValueUtils.isPrimitive(object) || visited.contains(object) || !filter.test(object)) {
            return;
        }
        result.add(new ModelAndPath(object, Utils.join(path, Objects::toString, ".")));
        visited.add(object);
        if (object instanceof Collection<?> collection) {
            int index = 0;
            for (Object item : collection) {
                path.addLast(index + "");
                getReachableObjects0(item, filter, ignoreTransientFields, path, visited, result);
                path.removeLast();
                index++;
            }
        } else {
            for (EntityProp prop : DescStore.get(object.getClass()).getProps()) {
                if (prop.isAccessible() && !(ignoreTransientFields && prop.isTransient())) {
                    path.addLast(prop.getName());
                    getReachableObjects0(prop.get(object), filter, ignoreTransientFields, path, visited, result);
                    path.removeLast();
                }
            }
        }
    }

    public static List<org.metavm.util.Reference> extractReferences(Collection<Object> objects, Predicate<Object> filter) {
        List<org.metavm.util.Reference> result = new LinkedList<>();
        Set<Object> visited = new IdentitySet<>();
        for (Object object : objects) {
            extractReferences0(object, visited, filter, result);
        }
        return result;
    }

    private static void extractReferences0(Object object, Set<Object> visited, Predicate<Object> filter, List<org.metavm.util.Reference> result) {
        if (object == null || ValueUtils.isPrimitive(object) || ValueUtils.isJavaType(object)
                || !filter.test(object) || visited.contains(object)) {
            return;
        }
        visited.add(object);
        for (EntityProp prop : DescStore.get(object.getClass()).getProps()) {
            if (prop.isAccessible()) {
                Object fieldValue = prop.get(object);
                if (fieldValue != null && !ValueUtils.isPrimitive(fieldValue) && !ValueUtils.isJavaType(fieldValue)) {
                    result.add(new Reference(object, prop.getField().getName(), fieldValue));
                    extractReferences0(fieldValue, visited, filter, result);
                }
            }
        }
    }

    public static Set<Class<?>> getModelClasses() {
        Reflections reflections =
                new Reflections(new ConfigurationBuilder().forPackages("org.metavm"));
        Set<Class<? extends Entity>> entitySubTypes = reflections.getSubTypesOf(Entity.class);
        Set<Class<?>> entityTypes = reflections.getTypesAnnotatedWith(org.metavm.api.Entity.class);
        var nativeValueClasses = reflections.getSubTypesOf(NativeEphemeralObject.class);
        var builtinClasses = Utils.filterAndMapUnique(List.of(StdKlass.values()), StdKlass::isAutoDefine, StdKlass::getJavaClass);
        return Utils.filterUnique(
                Utils.mergeSets(List.of(entitySubTypes, entityTypes, nativeValueClasses, builtinClasses)),
                klass -> !isCompiled(klass) && !klass.isAnonymousClass()
        );
    }

    public static void ensureProxyInitialized(Object object) {
        if (object instanceof ProxyObject proxyObject) {
            try {
                EntityMethodHandler<?> handler = EntityProxyFactory.getHandler(proxyObject);
                handler.ensureInitialized(object);
            } catch (ClassCastException e) {
                if (EntityProxyFactory.isDummy(object)) {
                    throw new InternalException("Trying to initialize a dummy object: " + object
                            + ", dummy source: " + EntityUtils.getEntityDesc(EntityProxyFactory.getDummyExtra(object))
                    );
                } else
                    throw e;
            }
        }
    }

    public static boolean isModelInitialized(Object object) {
        if (object instanceof ProxyObject proxyObject) {
            EntityMethodHandler<?> handler = EntityProxyFactory.getHandler(proxyObject);
            return handler.isInitialized();
        } else {
            return true;
        }
    }

    public static boolean isPrimitive(Class<?> klass) {
        return PRIM_CLASSES.contains(klass);
    }

    public static Id tryGetId(Object entity) {
        if (entity instanceof Identifiable identifiable) {
            return identifiable.tryGetId();
        }
        return null;
    }

    public static Long tryGetPhysicalId(Object entity) {
        return Utils.safeCall(tryGetId(entity), Id::tryGetTreeId);
    }

    @SuppressWarnings("unused")
    public static List<Entity> getAllEntities(Entity entity) {
        List<Entity> allEntities = new ArrayList<>();
        traverse(entity, allEntities::add);
        return allEntities;
    }

    public static void traverse(Entity entity, Consumer<Entity> action) {
        traverse(entity, action, new IdentitySet<>());
    }

    private static void traverse(Entity entity, Consumer<Entity> action, IdentitySet<Entity> visited) {
        if (entity == null || visited.contains(entity)) {
            return;
        }
        visited.add(entity);
        action.accept(entity);
        EntityDesc desc = DescStore.get(entity.getClass());
        for (EntityProp prop : desc.getProps()) {
            if (prop.isNull(entity)) {
                continue;
            }
            if (prop.isEntity(entity)) {
                traverse(prop.getEntity(entity), action, visited);
            } else if (prop.isEntityList(entity)) {
                for (Entity ref : prop.getEntityList(entity)) {
                    traverse(ref, action, visited);
                }
            } else if (prop.isEntityMap(entity)) {
                for (Entity ref : prop.getEntityMap(entity).values()) {
                    traverse(ref, action, visited);
                }
            }
        }
    }

    public static <T extends Entity> T makeDummyRef(Class<T> entityType, Id id) {
        return EntityProxyFactory.makeEntityDummy(entityType, id);
    }

    public static <T> Class<?> getEntityType(T entity) {
        return getEntityType(entity.getClass());
    }

    public static Class<? extends Entity> getRealEntityType(Entity entity) {
        return getRealType(entity.getClass()).asSubclass(Entity.class);
    }

    public static Class<?> getRealType(Object model) {
        return getRealType(model.getClass());
    }

    public static Class<?> getRealType(Class<?> type) {
        if (ProxyObject.class.isAssignableFrom(type)) {
            return type.getSuperclass();
        }
        return type;
    }

    public static Type getEntityType(Type type) {
//        type = ReflectUtils.eraseType(type);
        if (type instanceof Class<?> klass) {
            if (klass.getSuperclass() != null && klass.getSuperclass().isEnum())
                type = klass.getSuperclass();
        }
        return type;
    }

    public static Class<?> getEntityType(Class<?> type) {
        if (ProxyObject.class.isAssignableFrom(type)) {
            return type.getSuperclass();
        } else {
            return type;
        }
//        Class<?> tmp = type;
//        while (tmp.getSuperclass() != Entity.class && tmp != Object.class) {
//            if(tmp.isAnnotationPresent(EntityType.class)) {
//                return tmp.asSubclass(Entity.class);
//            }
//            tmp = tmp.getSuperclass();
//        }
//        if (tmp == Object.class) {
//            throw new RuntimeException("Class '" + type.getName() + "' is not an entity type");
//        }
//        return tmp.asSubclass(Entity.class);
    }

    @SuppressWarnings("unused")
    private static boolean isEntity(Class<?> klass) {
        return Entity.class.isAssignableFrom(klass) || ENTITY_CLASSES.contains(klass);
    }

    public static <T> List<T> newList(List<? extends T> prototype) {
        if (prototype instanceof LinkedList) {
            return new LinkedList<>();
        }
        if (prototype instanceof java.util.LinkedList) {
            return new java.util.LinkedList<>();
        } else {
            return new ArrayList<>();
        }
    }

    public static <K, V> Map<K, V> newMap(Map<? extends K, ? extends V> prototype) {
        if (prototype instanceof TreeMap) {
            return new TreeMap<>();
        } else if (prototype instanceof LinkedHashMap) {
            return new LinkedHashMap<>();
        } else {
            return new HashMap<>();
        }
    }

    public static <T> Set<T> newSet(Set<? extends T> prototype) {
        if (prototype instanceof TreeSet) {
            return new TreeSet<>();
        } else if (prototype instanceof LinkedHashSet) {
            return new LinkedHashSet<>();
        } else {
            return new HashSet<>();
        }
    }

    public static String getEntityDesc(Object entity) {
        return switch (entity) {
            case org.metavm.flow.Method method -> method.toString();
            case Function func -> func.toString();
            case Klass klass -> klass.toString();
            default -> getRealType(entity).getSimpleName() + "-" + entity;
        };
    }

    public static String getEntityPath(Object entity) {
        if (entity instanceof Entity e) {
            var list = new LinkedList<Entity>();
            var e1 = e;
            while (e1 != null) {
                list.addFirst(e1);
                e1 = e1.getParentEntity();
            }
            return Utils.join(list, EntityUtils::getEntityDesc, "/");
        } else
            return getEntityDesc(entity);
    }


    public static Object getParent(Object entity) {
        return entity instanceof Entity e ? e.getParentEntity() : null;
    }

}
