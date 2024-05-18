package tech.metavm.entity;

import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Klass;
import tech.metavm.util.*;

import java.lang.reflect.Type;
import java.util.*;

public abstract class PojoDef<T> extends ModelDef<T, ClassInstance> {
    private final PojoDef<? super T> superDef;
    private final List<IFieldDef> fieldDefList = new ArrayList<>();
    private final List<IndexConstraintDef> indexConstraintDefList = new ArrayList<>();
    private final List<CheckConstraintDef> checkConstraintDefList = new ArrayList<>();
    private final Map<Klass, PojoDef<? extends T>> subTypeDefList = new HashMap<>();
    protected final Klass klass;
    private Long id;
    private final DefContext defContext;

    public PojoDef(Class<T> javaClass,
                   Type javaType,
                   PojoDef<? super T> superDef,
                   Klass klass,
                   DefContext defContext
    ) {
        super(javaClass, javaType, ClassInstance.class);
        this.klass = klass;
        this.defContext = defContext;
        this.superDef = superDef;
        if (superDef != null) {
            superDef.addSubTypeDef(this);
        }
    }

    void addFieldDef(IFieldDef fieldDef) {
        fieldDefList.add(fieldDef);
    }

    void addUniqueConstraintDef(IndexConstraintDef indexConstraintDef) {
        this.indexConstraintDefList.add(indexConstraintDef);
    }

    void addCheckConstraintDef(CheckConstraintDef checkConstraintDef) {
        this.checkConstraintDefList.add(checkConstraintDef);
    }

    @Override
    public void initEntity(T model, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        if (klass.isType(instance.getType())) {
            if (model instanceof Entity entity) {
                entity.setParent(
                        NncUtils.get(instance.getParent(), p -> objectInstanceMap.getEntity(Entity.class, p)),
                        NncUtils.get(instance.getParentField(), defContext::getJavaField)
                );
            }
            setPojoFields(model, instance, objectInstanceMap);
        } else {
            PojoDef<? extends T> subTypeDef = getSubTypeDef(instance.getType().resolve());
            subTypeDef.initEntityHelper(model, instance, objectInstanceMap);
        }
    }

    @Override
    public void updateEntity(T pojo, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        setPojoFields(pojo, instance, objectInstanceMap);
    }

    private void setPojoFields(T pojo, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        if (superDef != null) {
            superDef.setPojoFields(pojo, instance, objectInstanceMap);
        }
        setFieldValues(pojo, instance, objectInstanceMap);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void initInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap) {
        instance.setType(instanceMap.getType(EntityUtils.getRuntimeType(model)));
        resetInstance(instance, model, instanceMap);
    }

    @Override
    public void updateInstance(ClassInstance instance, T model, ObjectInstanceMap instanceMap) {
        instance.ensureLoaded();
        resetInstance(instance, model, instanceMap);
    }

    private void resetInstance(ClassInstance instance, Object model, ObjectInstanceMap instanceMap) {
//        try(var ignored = ContextUtil.getProfiler().enter("PojoDef.resetInstance")) {
            Klass instanceKlass = instance.getKlass();
            if (klass.isType(instance.getType())) {
                if (model instanceof Entity entity)
                    Instances.reloadParent(entity, instance, instanceMap, defContext);
                instance.setType(klass.getType());
                instance.reset(getInstanceFields(model, instanceMap), instance.getVersion(), instance.getSyncVersion());
            } else {
                getSubTypeDef(instanceKlass).resetInstance(instance, model, instanceMap);
            }
//        }
    }

    protected Id getId(T model) {
        return null;
    }

    private PojoDef<? extends T> getSubTypeDef(Klass subType) {
        if (subType == klass ||
                !klass.isAssignableFrom(subType)) {
            throw new InternalException("type: " + subType + " is not a sub type of current type: " + getEntityClass());
        }
        Klass t = subType;
        while (t != null && Objects.equals(t.getSuperType(), klass.getType())) {
            t = NncUtils.get(t.getSuperType(), ClassType::resolve);
        }
        Klass directSubType = NncUtils.requireNonNull(t);
        PojoDef<? extends T> subDef = subTypeDefList.get(directSubType);
        if (subDef == null) {
            subDef = new TypeReference<PojoDef<? extends T>>() {
            }.getType().cast(defContext.getDef(directSubType));
        }
        return subDef;
    }

    protected Map<Field, Instance> getInstanceFields(Object object, ObjectInstanceMap instanceMap) {
//        try(var ignored = ContextUtil.getProfiler().enter("PojoDef.getInstanceFields")) {
            Map<Field, Instance> fieldData = new HashMap<>();
            if (superDef != null)
                fieldData.putAll(superDef.getInstanceFields(object, instanceMap));
            for (IFieldDef fieldDef : fieldDefList) {
                fieldData.put(fieldDef.getField(), fieldDef.getInstanceFieldValue(object, instanceMap));
            }
            return fieldData;
//        }
    }

    public <S extends T> void addSubTypeDef(PojoDef<S> def) {
        subTypeDefList.put(def.getTypeDef(), def);
    }

    public IFieldDef getFieldDef(java.lang.reflect.Field javaField) {
        var fieldDef = NncUtils.find(fieldDefList, fd -> fd.getJavaField().equals(javaField));
        if (fieldDef != null) {
            return fieldDef;
        }
        if (superDef != null) {
            return superDef.getFieldDef(javaField);
        } else {
            throw new InternalException("Can not find FieldDef for java field '" +
                    javaField.getDeclaringClass().getName() + "." + javaField.getName() + "'");
        }
    }

    public IndexConstraintDef getIndexConstraintDef(IndexDef<?> indexDef) {
        return NncUtils.findRequired(
                indexConstraintDefList,
                ucd -> ucd.getIndexDef() == indexDef
        );
    }

    @SuppressWarnings("unused")
    public List<IFieldDef> getFieldDefList() {
        return fieldDefList;
    }

    private void setFieldValues(Object entity, ClassInstance instance, ObjectInstanceMap objectInstanceMap) {
        for (IFieldDef fieldDef : fieldDefList) {
            fieldDef.setModelField(entity, instance, objectInstanceMap);
        }
    }

    public Field getFieldByJavaFieldName0(String javaFieldName) {
        IFieldDef fieldDef = NncUtils.find(
                fieldDefList,
                field -> field.getJavaField().getName().equals(javaFieldName)
        );
        if (fieldDef != null) {
            return fieldDef.getField();
        }
        return NncUtils.get(superDef, p -> p.getFieldByJavaFieldName0(javaFieldName));
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
        return ReflectionUtils.allocateInstance(proxyClass);
    }

    @SuppressWarnings("unused")
    public PojoDef<? super T> getSuperDef() {
        return superDef;
    }

    public Klass getTypeDef() {
        return klass;
    }

    public ClassType getType() {
        return klass.getType();
    }

    public Klass getKlass() {
        return klass;
    }
}
