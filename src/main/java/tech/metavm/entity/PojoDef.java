package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PojoDef<T> extends ModelDef<T, Instance> {
    private final String name;
    protected final Class<T> entityType;
    private final PojoDef<? super T> parentDef;
    private final List<FieldDef> fieldDefList = new ArrayList<>();
    private final List<UniqueConstraintDef> uniqueConstraintDefList = new ArrayList<>();
    private final Map<Type, PojoDef<? extends T>> subTypeDefList = new HashMap<>();
    protected final Type type;
    private Long id;
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
//        this.id = id;
        this.name = NncUtils.firstNonNull(
                name,
                NncUtils.get(entityAnnotation, EntityType::value),
                NncUtils.get(valueAnnotation, ValueType::value),
                entityType.getName()
        );
        this.entityType = entityType;
        this.parentDef = parentDef;
        this.type = type;
        this.defMap = defMap;
        if(parentDef != null) {
            parentDef.addSubTypeDef(this);
        }
    }

    void addFieldDef(FieldDef fieldDef) {
        fieldDefList.add(fieldDef);
    }

    void addUniqueConstraintDef(UniqueConstraintDef uniqueConstraintDef) {
        this.uniqueConstraintDefList.add(uniqueConstraintDef);
    }

    @Override
    public void initModel(T model, Instance instance, ModelInstanceMap modelInstanceMap) {
        if (type == instance.getType()) {
            setPojoFields(model, instance, modelInstanceMap);
            afterPojoCreated(model, instance);
        }
        else {
            PojoDef<? extends T> subTypeDef = getSubTypeDef(instance.getType());
            subTypeDef.initModelHelper(model, instance, modelInstanceMap);
        }
    }

    @Override
    public void updateModel(T pojo, Instance instance, ModelInstanceMap modelInstanceMap) {
        setPojoFields(pojo, instance, modelInstanceMap);
    }

    private void setPojoFields(T pojo, Instance instance, ModelInstanceMap modelInstanceMap) {
        if (parentDef != null) {
            parentDef.setFieldValues(pojo, instance, modelInstanceMap);
        }
        setFieldValues(pojo, instance, modelInstanceMap);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void initInstance(Instance instance, T model, ModelInstanceMap instanceMap) {
        if (type == instance.getType()) {
            instance.initialize(getInstanceFields(model, instanceMap));
        }
        else {
            getSubTypeDef(instance.getType()).initInstanceHelper(instance, model, instanceMap);
        }
    }

    protected Long getId(T model) {
        return null;
    }

    @Override
    public void updateInstance(T model, Instance instance, ModelInstanceMap instanceMap) {
        if (type == instance.getType()) {
            getInstanceFields(model, instanceMap).forEach(instance::set);
        }
        else {
            PojoDef<? extends T> subTypeDef = getSubTypeDef(instance.getType());
            subTypeDef.updateInstanceHelper(model, instance, instanceMap);
        }
    }

    private PojoDef<? extends T> getSubTypeDef(Type subType) {
        if(subType == type ||
                !type.isAssignableFrom(subType)) {
            throw new InternalException("type: " + subType + " is not a sub type of current type: " + entityType);
        }
        Type t = subType;
        while(t != null && t.getSuperType() != type) {
            t = t.getSuperType();
        }
        Type directSubType = NncUtils.requireNonNull(t);
        PojoDef<? extends T> subDef = subTypeDefList.get(directSubType);
        if(subDef == null) {
            subDef = new TypeReference<PojoDef<? extends T>>() {}.getType().cast(defMap.getDef(directSubType));
        }
        return subDef;
    }

    protected Map<Field, Object> getInstanceFields(Object object, ModelInstanceMap instanceMap) {
        Map<Field, Object> fieldData = new HashMap<>();
        if(parentDef != null) {
            fieldData.putAll(parentDef.getInstanceFields(object, instanceMap));
        }
        for (FieldDef fieldDef : fieldDefList) {
            fieldData.put(fieldDef.getField(), fieldDef.getInstanceFieldValue(object, instanceMap));
        }
        return fieldData;
    }

    public <S extends T> void addSubTypeDef(PojoDef<S> def) {
        subTypeDefList.put(def.getType(), def);
    }

    public FieldDef getFieldDef(java.lang.reflect.Field javaField) {
        return NncUtils.findRequired(fieldDefList, fieldDef -> fieldDef.getReflectField().equals(javaField));
    }

    public UniqueConstraintDef getUniqueConstraintDef(IndexDef<?> indexDef) {
        return NncUtils.findRequired(
                uniqueConstraintDefList,
                ucd -> ucd.getIndexDef() == indexDef
        );
    }

    @Override
    public Map<Object, Identifiable> getEntityMapping() {
        Map<Object, Identifiable> mapping = new HashMap<>();
        mapping.put(entityType, type);
        mapping.put(ArrayIdentifier.typeFields(entityType), type.getDeclaredFields());
        mapping.put(ArrayIdentifier.typeConstraints(entityType), type.getDeclaredConstraints());
        for (FieldDef fieldDef : fieldDefList) {
            mapping.put(fieldDef.getReflectField(), fieldDef.getField());
        }
        for (UniqueConstraintDef uniqueConstraintDef : uniqueConstraintDefList) {
            mapping.put(uniqueConstraintDef.getIndexDefField(), uniqueConstraintDef.getUniqueConstraint());
        }
        return mapping;
    }

    @SuppressWarnings("unused")
    public List<FieldDef> getFieldDefList() {
        return fieldDefList;
    }

    protected void afterPojoCreated(T pojo, IInstance instance) {}

    private void setFieldValues(Object entity, Instance instance, ModelInstanceMap modelInstanceMap) {
        for (FieldDef fieldDef : fieldDefList) {
            fieldDef.setModelField(entity, instance, modelInstanceMap);
        }
    }

    public Field getFieldByJavaFieldName(String javaFieldName) {
        return NncUtils.findRequired(
                fieldDefList,
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

    public Class<T> getModelType() {
        return entityType;
    }

    public String getName() {
        return name;
    }


}
