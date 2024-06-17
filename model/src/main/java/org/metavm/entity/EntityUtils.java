package org.metavm.entity;

import javassist.util.proxy.ProxyObject;
import org.metavm.api.*;
import org.metavm.flow.Flow;
import org.metavm.flow.Function;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.metavm.util.ReflectionUtils.*;

public class EntityUtils {

    public static final long MAXIMUM_DIFF_DEPTH = 1000;

    public static final Set<Class<?>> PRIM_CLASSES = Set.of(
            Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, String.class, Date.class, LocalDateTime.class,
            BigInteger.class, BigDecimal.class, Class.class, Object.class
    );

    public static final Set<Class<?>> ENTITY_CLASSES = Set.of(
            Klass.class, org.metavm.object.type.Field.class, Instance.class,
            EnumConstantRT.class
    );

    public static void clearIdRecursively(Object model) {
        traverseModelGraph(model, (path, o) -> {
            if (o instanceof IdInitializing idInitializing) {
                idInitializing.clearId();
            }
        });
    }

    public static Type getRuntimeType(Object object) {
        if (object instanceof RuntimeGeneric runtimeGeneric)
            return runtimeGeneric.getGenericType();
        else
            return object.getClass();
    }

    public static void forEachDescendant(Object object, Consumer<Object> action) {
        forEachDescendant(object, action, false);
    }

    public static void forEachDescendant(Object object, Consumer<Object> action, boolean skipCopyIgnore) {
        if (object instanceof Entity entity)
            entity.forEachDescendant(action::accept, skipCopyIgnore);
        else
            action.accept(object);
    }

    public static Object getRoot(Object object) {
        if (object instanceof Entity entity) {
            if (entity.getParentEntity() == null)
                return entity;
            else
                return getRoot(entity.getParentEntity());
        } else
            return object;
    }

    public static boolean isMapper(Object object) {
        return object instanceof Function func && func.getName().startsWith("map");
    }

    public static boolean isDurable(Object object) {
        return !isEphemeral(object);
    }

    public static boolean isEphemeral(Object object) {
        return object instanceof Entity entity && entity.isEphemeralEntity();
    }

    public static boolean isOrphaned(Object object) {
        if(object instanceof Entity entity) {
            if(entity.getParentEntity() != null) {
                if(entity.getParentEntityField() == null)
                    return !((ReadonlyArray<?>) entity.getParentEntity()).contains(entity);
                else
                    return ReflectionUtils.get(entity.getParentEntity(), entity.getParentEntityField()) != entity;
            }
        }
        return false;
    }

    public static void traverseModelGraph(Object model, BiConsumer<List<String>, Object> action) {
        traverseModelGraph0(model, action, new LinkedList<>(), new IdentitySet<>());
    }

    private static void traverseModelGraph0(Object model,
                                            BiConsumer<List<String>, Object> action,
                                            LinkedList<String> path,
                                            IdentitySet<Object> visited) {
        if (model == null || visited.contains(model)
                || isPrimitive(model.getClass())) {
            return;
        }
        visited.add(model);
        action.accept(path, model);
        Class<?> realClass = getRealType(model.getClass());
        EntityDesc desc = DescStore.get(realClass);
        for (EntityProp prop : desc.getProps()) {
            if (prop.isAccessible() && !prop.isTransient()) {
                path.addLast(prop.getName());
                traverseModelGraph0(prop.get(model), action, path, visited);
                path.removeLast();
            }
        }
    }

    public static void visitGraph(Object object, Consumer<Object> action) {
        visitGraph(List.of(object), action);
    }

    public static void visitGraph(Collection<?> objects, Consumer<Object> action) {
        var visited = new IdentitySet<>();
        objects.forEach(object -> {
            if(DebugEnv.recordPath) {
                EntityUtils.enterPath();
                visitGraph(object, visited, action);
                EntityUtils.exitPath();
            }
            else
                visitGraph(object, visited, action);
        });
    }

    private static final LinkedList<LinkedList<String>> paths = new LinkedList<>();

    private static LinkedList<String> path() {
        return Objects.requireNonNull(paths.peek());
    }

    public static String currentPath() {
        return String.join(".", path());
    }

    public static void enterPathItem(String item) {
        path().addLast(item);
    }

    public static void exitPathItem() {
        path().removeLast();
    }

    public static void enterPath() {
        paths.addLast(new LinkedList<>());
    }

    public static void exitPath() {
        paths.removeLast();
    }

    private static void visitGraph(Object object, IdentitySet<Object> visited, Consumer<Object> action) {
        if (visited.add(object)) {
            action.accept(object);
            if (object instanceof Entity entity)
                entity.forEachReference(o -> visitGraph(o, visited, action));
        }
    }

    public static <T extends Entity> boolean entityEquals(T entity1, T entity2) {
        if (entity1 == entity2) {
            return true;
        }
        if (entity1 == null || entity2 == null) {
            return false;
        }
        if (entity1.tryGetId() != null && entity2.tryGetId() != null) {
            return Objects.equals(entity1.tryGetId(), entity2.tryGetId());
        }
        return false;
    }

    public static List<Field> getIndexDefFields(Class<?> klass) {
        return getDeclaredStaticFields(
                klass,
                f -> f.getType() == IndexDef.class
        );
    }


    public static String getMetaFieldName(Class<?> klass, String javaFieldName) {
        return getMetaFieldName(getField(klass, javaFieldName));
    }

    public static String getMetaFieldName(Field javaField) {
        if (ENUM_NAME_FIELD.equals(javaField)) {
            return "name";
        }
        if (ENUM_ORDINAL_FIELD.equals(javaField)) {
            return "ordinal";
        }
        EntityField entityField = javaField.getAnnotation(EntityField.class);
        ChildEntity childEntity = javaField.getAnnotation(ChildEntity.class);
        return NncUtils.firstNonBlank(
                NncUtils.get(entityField, EntityField::value),
                NncUtils.get(childEntity, ChildEntity::value),
                javaField.getName()
        );
    }

    public static String getMetaConstraintName(Field field) {
        var anno = field.getAnnotation(EntityConstraint.class);
        return anno != null && !anno.value().isEmpty() ? anno.value() : field.getName();
    }

    public static String getMetaFlowName(Method method) {
        var anno = method.getAnnotation(EntityFlow.class);
        return anno != null && !anno.value().isEmpty() ? anno.value() : method.getName();
    }

    public static String getMetaEnumConstantName(Enum<?> enumConstant) {
        var field = getField(enumConstant.getDeclaringClass(), enumConstant.name());
        var anno = field.getAnnotation(EnumConstant.class);
        return anno != null && !anno.value().isEmpty() ? anno.value() : enumConstant.name();
    }

    public static String getMetaTypeName(Class<?> javaType) {
        var entityType = javaType.getAnnotation(EntityType.class);
        var valueType = javaType.getAnnotation(ValueType.class);
        var entityStruct = javaType.getAnnotation(EntityStruct.class);
        var valueStruct = javaType.getAnnotation(ValueStruct.class);
        return NncUtils.firstNonBlank(
                NncUtils.get(entityType, EntityType::value),
                NncUtils.get(valueType, ValueType::value),
                NncUtils.get(entityStruct, EntityStruct::value),
                NncUtils.get(valueStruct, ValueStruct::value),
                javaType.getSimpleName()
        );
    }


    private static boolean isCompiled(Class<?> klass) {
        var entityType = klass.getAnnotation(EntityType.class);
        if (entityType != null && entityType.compiled()) {
            return true;
        }
        var valueType = klass.getAnnotation(ValueType.class);
        return valueType != null && valueType.compiled();
    }

    public static String getMetaTypeVariableName(TypeVariable<?> typeVariable) {
        var anno = typeVariable.getAnnotation(TemplateVariable.class);
        return anno != null ? anno.value() : typeVariable.getName();
    }

    public static Method tryGetMethodByName(Class<?> klass, String methodName) {
        return getMethodByName(klass, methodName, false);
    }


    public static boolean isIndexDefField(Field field) {
        return Modifier.isStatic(field.getModifiers()) && field.getType() == IndexDef.class;
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
        result.add(new ModelAndPath(object, NncUtils.join(path, Objects::toString, ".")));
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

    public static Map<Object, List<org.metavm.util.Reference>> buildInvertedIndex(Collection<Object> objects, Predicate<Object> filter) {
        var references = extractReferences(objects, filter);
        Map<Object, List<org.metavm.util.Reference>> index = new IdentityHashMap<>();
        for (var reference : references) {
            index.computeIfAbsent(reference.target(), t -> new ArrayList<>()).add(reference);
        }
        return index;
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
        Set<Class<?>> entityTypes = reflections.getTypesAnnotatedWith(EntityType.class);
        var builtinClasses = NncUtils.filterAndMapUnique(BuiltinKlasses.defs(), BuiltinKlassDef::isAutoDefine, BuiltinKlassDef::getJavaClass);
        return NncUtils.filterUnique(
                NncUtils.mergeSets(entitySubTypes, entityTypes, builtinClasses),
                klass -> !isCompiled(klass)
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

    public static void ensureTreeInitialized(Object object) {
        forEachDescendant(object, EntityUtils::ensureProxyInitialized);
    }

    public static EntityMethodHandler.State getProxyState(Object object) {
        if (object instanceof ProxyObject proxyObject) {
            var handler = EntityProxyFactory.getHandler(proxyObject);
            return handler.getState();
        } else
            throw new InternalException(String.format("%s is not a proxy object", object));
    }

    public static void setProxyState(Object object, EntityMethodHandler.State state) {
        if (!trySetProxyState(object, state))
            throw new InternalException(String.format("%s is not a proxy object", object));
    }

    public static boolean trySetProxyState(Object object, EntityMethodHandler.State state) {
        if (object instanceof ProxyObject proxyObject) {
            var handler = EntityProxyFactory.getHandler(proxyObject);
            handler.setState(state);
            return true;
        } else
            return false;
    }

    public static boolean isModelInitialized(Object object) {
        if (object instanceof ProxyObject proxyObject) {
            EntityMethodHandler<?> handler = EntityProxyFactory.getHandler(proxyObject);
            return handler.isInitialized();
        } else {
            return true;
        }
    }

    public static Class<?> getRealClass(Class<?> klass1, Class<?> klass2) {
        if (klass1 == klass2) {
            return klass1;
        }
        if (klass1 == klass2.getSuperclass()) {
            if (ProxyObject.class.isAssignableFrom(klass2)) {
                return klass1;
            }
        }
        if (klass2 == klass1.getSuperclass()) {
            if (ProxyObject.class.isAssignableFrom(klass1)) {
                return klass2;
            }
        }
        return null;
    }

    public static boolean isPrimitive(Class<?> klass) {
        return PRIM_CLASSES.contains(klass);
    }

    private static boolean isEnum(Class<?> klass) {
        return Enum.class.isAssignableFrom(klass);
    }

    public static Id tryGetId(Object entity) {
        if (entity instanceof Identifiable identifiable) {
            return identifiable.tryGetId();
        }
        return null;
    }

    public static Long tryGetPhysicalId(Object entity) {
        return NncUtils.get(tryGetId(entity), Id::tryGetTreeId);
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
        if (entity instanceof Flow flow)
            return flow.getNameWithTypeArguments();
        if (entity instanceof ReadonlyArray<?> array)
            return getRealType(array.getClass()).getSimpleName();
        if(entity instanceof Klass klass)
            return klass.getTypeDesc();
        return entity.toString();
    }

    public static String getEntityPath(Object entity) {
        if (entity instanceof Entity e) {
            var list = new LinkedList<Entity>();
            var e1 = e;
            while (e1 != null) {
                list.addFirst(e1);
                e1 = e1.getParentEntity();
            }
            return NncUtils.join(list, EntityUtils::getEntityDesc, "/");
        } else
            return getEntityDesc(entity);
    }


    public static Object getParent(Object entity) {
        return entity instanceof Entity e ? e.getParentEntity() : null;
    }
}
