package tech.metavm.entity;

import tech.metavm.object.instance.*;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.ValueUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PojoDef<T> extends ModelDef<T, Instance> {
    /*private final long typeId;*/
    private final String name;
    protected final Class<T> entityType;
    private final PojoDef<? super T> parentDef;
    private final List<FieldDef> fieldDefs = new ArrayList<>();
    private final Map<Class<?>, PojoDef<? extends T>> subTypeDefs = new HashMap<>();
    protected final Type type;

    public PojoDef(/*long typeId,*/
                   String name,
                   Class<T> entityType,
                   @Nullable PojoDef<? super T> parentDef,
                   Type type
    ) {
        super(entityType, Instance.class);
        EntityType annotation = entityType.getAnnotation(EntityType.class);
        /*this.typeId = typeId;*/
        this.name = name != null ? name : (annotation != null ? annotation.value() : entityType.getSimpleName());
        this.entityType = entityType;
        this.parentDef = parentDef;
        this.type = initType(type);
        if(parentDef != null) {
            parentDef.addSubTypeDef(entityType, this);
        }
    }

    private Type initType(Type type) {
        if(type == null) {
            type = new Type(
                    name,
                    NncUtils.get(parentDef, PojoDef::getType),
                    ValueUtil.getTypeCategory(entityType)
            );
//            type.initId(typeId);
            return type;
        }
        else {
            type.setName(name);
            type.setSuperType(NncUtils.get(parentDef, PojoDef::getType));
            type.setCategory(ValueUtil.getTypeCategory(entityType));
            return type;
        }
    }

    void addFieldDef(FieldDef fieldDef) {
        fieldDefs.add(fieldDef);
    }

    @Override
    public T newModel(Instance instance, ModelMap modelMap) {
        if (this.entityType.equals(instance.getEntityType())) {
            T model = ReflectUtils.allocateInstance(entityType);
            setPojoFields(model, instance, modelMap);
            afterPojoCreated(model, instance);
            return model;
        }
        else {
            PojoDef<? extends T> subTypeDef = getSubTypeDef(entityType);
            return subTypeDef.newModel(instance, modelMap);
        }
    }

    @Override
    public void updateModel(T pojo, Instance instance, ModelMap modelMap) {
        setPojoFields(pojo, instance, modelMap);
    }

    private void setPojoFields(T pojo, Instance instance, ModelMap modelMap) {
        if (parentDef != null) {
            parentDef.setFieldValues(pojo, instance, modelMap);
        }
        setFieldValues(pojo, instance, modelMap);
    }

    @Override
    public Instance newInstance(T model, InstanceMap instanceMap) {
        Class<?> entityType = model.getClass();
        if (this.entityType.equals(entityType)) {
            return new Instance(
                    getInstanceFields(model, instanceMap),
                    type,
                    entityType
            );
        }
        else {
            PojoDef<? extends T> subTypeDef = getSubTypeDef(entityType);
            return subTypeDef.newInstanceHelper(model, instanceMap);
        }
    }

    protected Long getId(T model) {
        return null;
    }

    @Override
    public void updateInstance(T model, Instance instance, InstanceMap instanceMap) {
        Class<?> entityType = instance.getEntityType();
        if (entityType == null || this.entityType.equals(entityType)) {
            getInstanceFields(model, instanceMap).forEach(instance::set);
        }
        else {
            PojoDef<? extends T> subTypeDef = getSubTypeDef(entityType);
            subTypeDef.updateInstanceHelper(model, instance, instanceMap);
        }
    }

    private PojoDef<? extends T> getSubTypeDef(Class<?> subType) {
        Class<?> t = subType;
        while(t != null) {
            PojoDef<? extends T> subTypeDef = subTypeDefs.get(t);
            if(subTypeDef != null) {
                return subTypeDef;
            }
            t = t.getSuperclass();
        }
        throw new InternalException("Can not find defition for type: " + subType);
    }

    protected Map<Field, Object> getInstanceFields(Object object, InstanceMap instanceMap) {
        Map<Field, Object> fieldData = new HashMap<>();
        if(parentDef != null) {
            fieldData.putAll(parentDef.getInstanceFields(object, instanceMap));
        }
        for (FieldDef fieldDef : fieldDefs) {
            fieldData.put(fieldDef.getField(), fieldDef.getInstanceFieldValue(object, instanceMap));
        }
        return fieldData;
    }

    public <S extends T> void addSubTypeDef(Class<? extends S> subType, PojoDef<S> def) {
        subTypeDefs.put(subType.asSubclass(entityType), def);
    }

    @SuppressWarnings("unused")
    public List<FieldDef> getFieldDefs() {
        return fieldDefs;
    }

    protected void afterPojoCreated(T pojo, IInstance instance) {}

    private void setFieldValues(Object entity, IInstance instance, ModelMap modelMap) {
        for (FieldDef fieldDef : fieldDefs) {
            fieldDef.setField(entity, instance, modelMap);
        }
    }

    public Field getFieldByJavaFieldName(String javaFieldName) {
        return NncUtils.findRequired(
                fieldDefs,
                field -> field.getReflectField().getName().equals(javaFieldName)
        ).getField();
    }

    @SuppressWarnings("unused")
    public PojoDef<? super T> getParentDef() {
        return parentDef;
    }

    public Type getType() {
        return type;
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public String getName() {
        return name;
    }

}
