package tech.metavm.entity;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PojoDef<T> extends ModelDef<T, ClassInstance> {
    private final PojoDef<? super T> parentDef;
    private final List<IFieldDef> fieldDefList = new ArrayList<>();
    private final List<UniqueConstraintDef> uniqueConstraintDefList = new ArrayList<>();
    private final Map<ClassType, PojoDef<? extends T>> subTypeDefList = new HashMap<>();
    protected final ClassType type;
    private Long id;
    private final DefMap defMap;

    public PojoDef(Class<T> javaClass,
                   Type javaType,
                   @Nullable PojoDef<? super T> parentDef,
                   ClassType type,
                   DefMap defMap
    ) {
        super(javaClass, javaType, ClassInstance.class);
        this.parentDef = parentDef;
        this.type = type;
        this.defMap = defMap;
        if(parentDef != null) {
            parentDef.addSubTypeDef(this);
        }
    }

    void addFieldDef(IFieldDef fieldDef) {
        fieldDefList.add(fieldDef);
    }

    void addUniqueConstraintDef(UniqueConstraintDef uniqueConstraintDef) {
        this.uniqueConstraintDefList.add(uniqueConstraintDef);
    }

    @Override
    public void initModel(T model, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        if (type == instance.getType()) {
            setPojoFields(model, instance, modelInstanceMap);
        }
        else {
            PojoDef<? extends T> subTypeDef = getSubTypeDef(instance.getType());
            subTypeDef.initModelHelper(model, instance, modelInstanceMap);
        }
    }

    @Override
    public void updateModel(T pojo, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        setPojoFields(pojo, instance, modelInstanceMap);
    }

    private void setPojoFields(T pojo, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        if (parentDef != null) {
            parentDef.setPojoFields(pojo, instance, modelInstanceMap);
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
    public void initInstance(ClassInstance instance, T model, ModelInstanceMap instanceMap) {
        ClassType instanceType = instance.getType();
        if (type == instance.getType()) {
            instance.initialize(getInstanceFields(model, instanceMap));
        }
        else {
            getSubTypeDef(instanceType).initInstanceHelper(instance, model, instanceMap);
        }
    }

    protected Long getId(T model) {
        return null;
    }

    @Override
    public void updateInstance(ClassInstance instance, T model, ModelInstanceMap instanceMap) {
        if (type == instance.getType()) {
            getInstanceFields(model, instanceMap).forEach(instance::set);
        }
        else {
            PojoDef<? extends T> subTypeDef = getSubTypeDef(instance.getType());
            subTypeDef.updateInstanceHelper(model, instance, instanceMap);
        }
    }

    private PojoDef<? extends T> getSubTypeDef(ClassType subType) {
        if(subType == type ||
                !type.isAssignableFrom(subType)) {
            throw new InternalException("type: " + subType + " is not a sub type of current type: " + getJavaClass());
        }
        ClassType t = subType;
        while(t != null && t.getSuperType() != type) {
            t = t.getSuperType();
        }
        ClassType directSubType = NncUtils.requireNonNull(t);
        PojoDef<? extends T> subDef = subTypeDefList.get(directSubType);
        if(subDef == null) {
            subDef = new TypeReference<PojoDef<? extends T>>() {}.getType().cast(defMap.getDef(directSubType));
        }
        return subDef;
    }

    protected Map<Field, Instance> getInstanceFields(Object object, ModelInstanceMap instanceMap) {
        Map<Field, Instance> fieldData = new HashMap<>();
        if(parentDef != null) {
            fieldData.putAll(parentDef.getInstanceFields(object, instanceMap));
        }
        for (IFieldDef fieldDef : fieldDefList) {
            fieldData.put(fieldDef.getField(), fieldDef.getInstanceFieldValue(object, instanceMap));
        }
        return fieldData;
    }

    public <S extends T> void addSubTypeDef(PojoDef<S> def) {
        subTypeDefList.put(def.getType(), def);
    }

    public IFieldDef getFieldDef(java.lang.reflect.Field javaField) {
        return NncUtils.findRequired(fieldDefList, fieldDef -> fieldDef.getJavaField().equals(javaField));
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
        mapping.put(getJavaClass(), type);
        mapping.put(ModelIdentity.classTypeFields(getJavaClass()), type.getDeclaredFields());
        mapping.put(ModelIdentity.classTypeConstraints(getJavaClass()), type.getDeclaredConstraints());
        mapping.put(ModelIdentity.classTypeFlows(getJavaClass()), type.getDeclaredFlows());
        if(type.getNullableType() != null) {
            mapping.put(ModelIdentity.typeNullableType(getJavaClass()), type.getNullableType());
        }
        if(type.getArrayType() != null) {
            mapping.put(ModelIdentity.typeArrayType(getJavaClass()), type.getArrayType());
        }

        for (IFieldDef fieldDef : fieldDefList) {
            mapping.put(fieldDef.getJavaField(), fieldDef.getField());
        }
        for (UniqueConstraintDef uniqueConstraintDef : uniqueConstraintDefList) {
            mapping.put(uniqueConstraintDef.getIndexDefField(), uniqueConstraintDef.getUniqueConstraint());
        }
        return mapping;
    }

    @SuppressWarnings("unused")
    public List<IFieldDef> getFieldDefList() {
        return fieldDefList;
    }

    private void setFieldValues(Object entity, ClassInstance instance, ModelInstanceMap modelInstanceMap) {
        for (IFieldDef fieldDef : fieldDefList) {
            fieldDef.setModelField(entity, instance, modelInstanceMap);
        }
    }

    public Field getFieldByJavaFieldName0(String javaFieldName) {
        IFieldDef fieldDef = NncUtils.find(
                fieldDefList,
                field -> field.getJavaField().getName().equals(javaFieldName)
        );
        if(fieldDef != null) {
            return fieldDef.getField();
        }
        return NncUtils.get(parentDef, p -> p.getFieldByJavaFieldName0(javaFieldName));
    }

    public Field getFieldByJavaFieldName(String javaFieldName) {
        return NncUtils.requireNonNull(
                getFieldByJavaFieldName0(javaFieldName),
                "Can not find field for java field name '" + javaFieldName + "'"
        );
    }

    @Override
    public boolean isProxySupported() {
        return true;
    }

    @Override
    public T createModelProxy(Class<? extends T> proxyClass) {
        return ReflectUtils.allocateInstance(proxyClass);
    }

    @SuppressWarnings("unused")
    public PojoDef<? super T> getParentDef() {
        return parentDef;
    }

    public ClassType getType() {
        return type;
    }



}
