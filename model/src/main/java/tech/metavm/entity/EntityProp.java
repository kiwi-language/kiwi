package tech.metavm.entity;

import tech.metavm.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class EntityProp {

    private final Field field;
    private final boolean accessible;
    private final boolean _transient;
    private final boolean childEntity;
    private final boolean copyIgnore;

    public EntityProp(Field field) {
        this.field = field;
        accessible = field.trySetAccessible();
        _transient = Modifier.isTransient(field.getModifiers());
        childEntity = field.isAnnotationPresent(ChildEntity.class);
        copyIgnore = field.isAnnotationPresent(CopyIgnore.class);
    }

    public String getName() {
        return field.getName();
    }

    public boolean isAccessible() {
        return accessible;
    }

    public boolean isNull(Object entity) {
        return get(entity) == null;
    }

    public boolean isEntityList() {
        return ReflectionUtils.isCollectionOf(field.getGenericType(), Entity.class);
    }

    public boolean isEntity() {
        return Entity.class.isAssignableFrom(field.getType());
    }

    public boolean isEntity(Object entity) {
        return get(entity) instanceof Entity;
    }

    public boolean isTransient() {
        return _transient;
    }

    public boolean isCopyIgnore() {
        return copyIgnore;
    }

    public boolean isEntityList(Object entity) {
        Object propValue = get(entity);
        if(propValue instanceof Collection<?> coll) {
            for (Object item : coll) {
                if(item instanceof Entity) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEntityMap(Object entity) {
        Object propValue = get(entity);
        if(propValue instanceof Map map) {
            for (Object item : map.values()) {
                if(item instanceof Entity) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isChildEntity() {
        return childEntity;
    }

    public Entity getEntity(Object object) {
        return (Entity) ReflectionUtils.get(object, field);
    }

    public List<Entity> getEntityList(Object object) {
        List<Entity> entityList = new ArrayList<>();
        Collection<?> coll = (Collection<?>) get(object);

        for (Object item : coll) {
            if(item instanceof Entity entity) {
                entityList.add(entity);
            }
        }

        return entityList;
    }

    public Map<Object,Entity> getEntityMap(Object object) {
        Map<Object, Object> map = (Map) get(object);
        Map<Object, Entity> entityMap = new HashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if(e.getValue() instanceof Entity entity) {
                entityMap.put(e.getKey(), entity);
            }
        }
        return entityMap;
    }

    public Field getField() {
        return field;
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    public Object get(Object object) {
        return ReflectionUtils.get(object, field);
    }

    public void set(Object object, Object fieldValue) {
        ReflectionUtils.set(object, field, fieldValue);
    }

    @Override
    public String toString() {
        return field.getDeclaringClass().getSimpleName() + "." + field.getName();
    }
}
