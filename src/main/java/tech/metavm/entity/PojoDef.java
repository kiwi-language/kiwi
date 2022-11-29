package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
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
    private final String name;
    protected final Class<T> entityType;
    private final PojoDef<? super T> parentDef;
    private final List<FieldDef> fieldDefs = new ArrayList<>();
    private final Map<Class<? extends T>, PojoDef<? extends T>> subTypeDefs = new HashMap<>();
    private final Map<java.lang.reflect.Type, ParameterizedPojoDef<? extends T>> parameterizedPojoDefs = new HashMap<>();
    protected final Type type;
    private final DefMap defMap;

    public PojoDef(String name,
                   Class<T> entityType,
                   @Nullable PojoDef<? super T> parentDef,
                   Type type,
                   DefMap defMap
    ) {
        super(entityType, Instance.class);
        EntityType entityAnnotation = entityType.getAnnotation(EntityType.class);
        ValueType valueAnnotation = entityType.getAnnotation(ValueType.class);
        this.name = NncUtils.firstNonNull(
                name,
                NncUtils.get(entityAnnotation, EntityType::value),
                NncUtils.get(valueAnnotation, ValueType::value),
                entityType.getName()
        );
        this.entityType = entityType;
        this.parentDef = parentDef;
        this.type = initType(type);
        this.defMap = defMap;
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
            PojoDef<? extends T> subTypeDef = getSubTypeDef(ReflectUtils.getRawClass(instance.getEntityType()));
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
        java.lang.reflect.Type entityType = instance.getEntityType();
        if (entityType == null || this.entityType.equals(entityType)) {
            getInstanceFields(model, instanceMap).forEach(instance::set);
        }
        else {
            PojoDef<? extends T> subTypeDef = getSubTypeDef(ReflectUtils.getRawClass(entityType));
            subTypeDef.updateInstanceHelper(model, instance, instanceMap);
        }
    }

    private PojoDef<? extends T> getSubTypeDef(Class<?> subType) {
        if(subType == entityType ||
                !entityType.isAssignableFrom(subType)) {
            throw new InternalException("type: " + subType + " is not a sub type of current type: " + entityType);
        }
        Class<?> t = subType;
        while(t.getSuperclass() != entityType) {
            t = t.getSuperclass();
        }
        Class<? extends T> directSubType = t.asSubclass(entityType);
        PojoDef<? extends T> subDef = subTypeDefs.get(directSubType);
        if(subDef == null) {
            subDef = defMap.getPojoDef(directSubType);
            defMap.putDef(directSubType, subDef);
        }
        return subDef;
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

    public void addParameterizedPojoDef(ParameterizedPojoDef<? extends T> parameterizedPojoType) {
        parameterizedPojoDefs.put(parameterizedPojoType.getGenericType(), parameterizedPojoType);
    }

    public FieldDef getFieldDef(java.lang.reflect.Field javaField) {
        return NncUtils.findRequired(fieldDefs, fieldDef -> fieldDef.getReflectField().equals(javaField));
    }

    @Override
    public Map<Object, Entity> getEntityMapping() {
        Map<Object, Entity> mapping = new HashMap<>();
        mapping.put(entityType, type);
        for (FieldDef fieldDef : fieldDefs) {
            mapping.put(fieldDef.getReflectField(), fieldDef.getField());
        }
        return mapping;
    }

    @SuppressWarnings("unused")
    public List<FieldDef> getFieldDefs() {
        return fieldDefs;
    }

    protected void afterPojoCreated(T pojo, IInstance instance) {}

    private void setFieldValues(Object entity, Instance instance, ModelMap modelMap) {
        for (FieldDef fieldDef : fieldDefs) {
            fieldDef.setModelField(entity, instance, modelMap);
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
