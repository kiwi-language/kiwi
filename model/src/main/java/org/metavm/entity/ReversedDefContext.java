package org.metavm.entity;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReversedDefContext extends DefContext {

    private static final Logger logger = LoggerFactory.getLogger(ReversedDefContext.class);

    private final SystemDefContext defContext;
    private final Map<TypeDef, ModelDef<?>> typeDef2Def = new HashMap<>();
    private final Map<java.lang.reflect.Type, ModelDef<?>> javaType2Def = new HashMap<>();
    private final Map<Integer, ModelDef<?>> typeTag2Def = new HashMap<>();

    public ReversedDefContext(IInstanceContext instanceContext, SystemDefContext defContext) {
        super(instanceContext);
        this.defContext = defContext;
    }

    @Override
    protected TypeFactory getTypeFactory() {
        return null;
    }

    @Override
    public TypeRegistry getTypeRegistry() {
        return defContext;
    }

    @Override
    public DefContext getDefContext() {
        return defContext;
    }

    @Nullable
    @Override
    protected ModelDef<?> tryGetDef(int typeTag) {
        return typeTag2Def.get(typeTag);
    }

    @Override
    public ModelDef<?> tryGetDef(TypeDef typeDef) {
        return typeDef2Def.get(typeDef);
    }

    @Override
    public ModelDef<?> getDef(java.lang.reflect.Type javaType, ResolutionStage stage) {
        return Objects.requireNonNull(javaType2Def.get(javaType), () -> "Cannot find def for type: " + javaType);
    }

    @Override
    public IEntityContext createSame(long appId) {
        return new ReversedDefContext(getInstanceContext(), defContext);
    }

    public void initializeFrom(SystemDefContext sysDefContext) {
        sysDefContext.getAllDefList().forEach(this::initDef);
        initFieldTargetMapper();
    }

    private void initFieldTargetMapper() {
        for (ModelDef<?> def : javaType2Def.values()) {
            if(def instanceof PojoDef<?> pojoDef) {
                for (IFieldDef fieldDef : pojoDef.getFieldDefList()) {
                    if(fieldDef instanceof FieldDef fd) {
                        var javaField = fd.getJavaField();
                        var javaFieldType = ReflectionUtils.evaluateFieldType(def.getJavaType(), javaField.getGenericType());
                        var fieldType = getType(javaFieldType);
                        if(!(fieldType instanceof PrimitiveType)) {
                            try {
                                fd.setTargetMapper(getMapper(javaFieldType));
                            }
                            catch (Exception e) {
                                logger.debug("Field type: {}", fieldType);
                                throw e;
                            }
                        }
                    }
                }
            }
        }
    }

    private void initDef(ModelDef<?> prototype) {
        var typeDef = getTypeDef(prototype.getTypeDef().getId());
        EntityUtils.ensureTreeInitialized(typeDef);
        var def = createDef(prototype);
        javaType2Def.put(prototype.getJavaType(), def);
        typeDef2Def.put(typeDef, def);
        if(typeDef instanceof Klass klass && TypeTags.isSystemTypeTag((int) klass.getTag()))
            typeTag2Def.put((int) klass.getTag(), def);
    }

    private ModelDef<?> createDef(ModelDef<?> prototype) {
        var existing = javaType2Def.get(prototype.getJavaType());
        if(existing != null)
            return existing;
        return switch (prototype) {
            case EntityDef<?> entityDef -> createEntityDef(entityDef);
            case ValueDef<?> valueDef -> createValueDef(valueDef);
            case RecordDef<?> recordDef -> createRecordDef(recordDef);
            case InterfaceDef<?> interfaceDef -> createInterfaceDef(interfaceDef);
            case EnumDef<?> enumDef -> createEnumDef(enumDef);
            case TypeVariableDef typeVariableDef -> createTypeVariableDef(typeVariableDef);
            case DirectDef<?> directDef -> createDirectDef(directDef);
            default -> throw new IllegalStateException("Unrecognized ModelDef: " + prototype);
        };
    }

    private TypeVariableDef createTypeVariableDef(TypeVariableDef prototype) {
        return new TypeVariableDef((TypeVariable<?>) prototype.getJavaType(),
                getTypeVariable(prototype.getVariable().getId()));
    }

    private <T extends Record> RecordDef<T> createRecordDef(RecordDef<T> prototype) {
        var klass = getKlass(prototype.getKlass().getId());
        var def = new RecordDef<>(prototype.getJavaClass(), prototype.getJavaType(), getSuperDef(prototype), klass, this);
        initFieldDefs(def, prototype);
        return def;
    }

    private <T extends Enum<?>> EnumDef<T> createEnumDef(EnumDef<T> prototype) {
        //noinspection rawtypes
        var superDef = (ValueDef) createDef(prototype.getParentDef());
        //noinspection unchecked
        var def = new EnumDef<T>(prototype.getEntityClass(), superDef, getKlass(prototype.getKlass().getId()), this);
        javaType2Def.put(def.getJavaType(), def);
        for (EnumConstantDef<T> enumConstantDef : prototype.getEnumConstantDefs()) {
            new EnumConstantDef<>(
                    enumConstantDef.getValue(),
                    def,
                    (ClassInstance) getInstanceContext().get(enumConstantDef.getInstance().getId())
            );
        }
        return def;
    }

    private <T> DirectDef<T> createDirectDef(DirectDef<T> prototype) {
        return new DirectDef<>(prototype.getJavaClass(), getTypeDef(prototype.getTypeDef().getId()));
    }

    private <T extends Entity> EntityDef<T> createEntityDef(EntityDef<T> prototype) {
        var klass = getKlass(prototype.getKlass().getId());
       var def = new EntityDef<>(prototype.getJavaClass(), prototype.getJavaType(), getSuperDef(prototype), klass, this);
       initFieldDefs(def, prototype);
       return def;
    }

    private <T> ValueDef<T> createValueDef(ValueDef<T> prototype) {
        var klass = getKlass(prototype.getKlass().getId());
        var def = new ValueDef<>(prototype.getJavaClass(), prototype.getEntityType(), getSuperDef(prototype), klass, this);
        initFieldDefs(def, prototype);
        return def;
    }

    private <T> InterfaceDef<T> createInterfaceDef(InterfaceDef<T> prototype) {
        return new InterfaceDef<>(prototype.getJavaClass(), prototype.getJavaType(), getSuperDef(prototype),
                getKlass(prototype.getKlass().getId()), this);
    }

    private <T> PojoDef<? super T> getSuperDef(PojoDef<T> prototype) {
        var s = prototype.getSuperDef();
        if(s == null)
            return null;
        //noinspection unchecked
        return (PojoDef<? super T>) createDef(s);
    }

    private void initFieldDefs(PojoDef<?> pojoDef, PojoDef<?> prototype) {
        javaType2Def.put(pojoDef.getJavaType(), pojoDef);
        for (var prototypeField : prototype.getKlass().getFields()) {
            var field = pojoDef.getKlass().getSelfField(f -> f.getCodeNotNull().equals(prototypeField.getCodeNotNull()));
            var javaField = ReflectionUtils.getField(pojoDef.getJavaClass(), field.getCodeNotNull());
            if (javaField.getType() == Class.class)
                new ClassFieldDef(pojoDef, field, javaField, this);
            else if(Value.class.isAssignableFrom(javaField.getType()))
                new InstanceFieldDef(javaField, field, pojoDef);
            else
                new FieldDef(field, field.isNullable(), javaField, pojoDef, null);
        }
    }

}
