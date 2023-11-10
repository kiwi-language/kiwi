package tech.metavm.entity;

import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class EntityProp {

    private final Field field;
    private final boolean accessible;

    public EntityProp(Field field) {
        this.field = field;
        accessible = field.trySetAccessible();
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
        return ReflectUtils.isCollectionOf(field.getGenericType(), Entity.class);
    }

    public boolean isEntity() {
        return Entity.class.isAssignableFrom(field.getType());
    }

    public boolean isEntity(Object entity) {
        return get(entity) instanceof Entity;
    }

    public boolean isTransient() {
        return Modifier.isTransient(field.getModifiers());
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
        if(!isEntity() && !isEntityList()) {
            return false;
        }
        return field.getClass().isAnnotationPresent(ChildEntity.class);
    }

    public Entity getEntity(Object object) {
        return (Entity) ReflectUtils.get(object, field);
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

    public Object get(Object object) {
        return ReflectUtils.get(object, field);
    }

    public void set(Object object, Object fieldValue) {
        ReflectUtils.set(object, field, fieldValue);
    }

}
