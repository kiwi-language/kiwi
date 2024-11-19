package org.metavm.entity;

import org.metavm.entity.natives.StandardStaticMethods;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.Function;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.Column;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Consumer;

public class ReversedDefContext extends DefContext {

    private static final Logger logger = LoggerFactory.getLogger(ReversedDefContext.class);

    private DefContext defContext;
    private final Map<TypeDef, ModelDef<?>> typeDef2Def = new HashMap<>();
    private final Map<java.lang.reflect.Type, ModelDef<?>> javaType2Def = new HashMap<>();
    private final Map<Integer, ModelDef<?>> typeTag2Def = new HashMap<>();
    private State state = State.PRE_INITIALIZING;
    private final List<Klass> extraKlasses = new ArrayList<>();
    private final List<Column> columns = new ArrayList<>();
    private final List<Function> stdFunctions = new ArrayList<>();
    private final Map<Object, Id> entity2Id = new IdentityHashMap<>();

    private enum State {
        PRE_INITIALIZING,
        INITIALIZING,
        POST_INITIALIZING
    }

    public ReversedDefContext(IInstanceContext instanceContext, DefContext defContext) {
        super(instanceContext);
        this.defContext = defContext;
    }

    @Override
    protected TypeFactory getTypeFactory() {
        return null;
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

    private void loadExtraKlasses(List<String> ids) {
        for (String id : ids) {
            var klass = getKlass(id);
            EntityUtils.ensureTreeInitialized(klass);
            extraKlasses.add(klass);
        }
    }

    @Override
    public ModelDef<?> getDef(java.lang.reflect.Type javaType, ResolutionStage stage) {
        checkJavaType(javaType);
        javaType = ReflectionUtils.getBoxedType(javaType);
        if (!(javaType instanceof TypeVariable<?>)) {
            javaType = EntityUtils.getEntityType(javaType);
            if (javaType instanceof Class<?> klass) {
                if (ReflectionUtils.isPrimitiveWrapper(klass))
                    javaType = BOX_CLASS_MAP.getOrDefault(klass, klass);
                else
                    javaType = EntityUtils.getRealType(klass);
            }
        }
        var javaTypeF = javaType;
        return Objects.requireNonNull(javaType2Def.get(javaType), () -> "Cannot find def for type: " + javaTypeF);
    }

    @Override
    public Collection<ModelDef<?>> getAllDefList() {
        return typeDef2Def.values();
    }

    @Override
    public boolean containsDef(TypeDef typeDef) {
        return typeDef2Def.containsKey(typeDef);
    }

    @Override
    public IEntityContext createSame(long appId) {
        return new ReversedDefContext(getInstanceContext(), defContext);
    }

    public void initializeFrom(DefContext sysDefContext, List<String> extraKlassIds) {
        state = State.INITIALIZING;
        loadStdFunctions();
        loadExtraKlasses(extraKlassIds);
        sysDefContext.getAllDefList().forEach(this::initDef);
        loadColumns(sysDefContext);
        initFieldTargetMapper();
        defContext = this;
        state = State.POST_INITIALIZING;
        recordEntityIds();
        postInitialization();
        postProcess();
    }

    public void initEnv() {
        StdFunction.initializeFromDefContext(this, true);
        StdKlass.initialize(this, true);
        StdMethod.initialize(this, true);
        StdField.initialize(this, true);
        StandardStaticMethods.initialize(this, true);
    }

    private void freezeKlasses() {
        typeDef2Def.keySet().forEach(t -> {
            if(t instanceof Klass k)
                k.freeze();
        });
    }

    private void postProcess() {
        freezeKlasses();
        initEnv();
        for (TypeDef typeDef : typeDef2Def.keySet()) {
            if(typeDef instanceof Klass klass)
                klass.rebuildMethodTable();
        }
    }

    private void loadStdFunctions() {
        for (StdFunction stdFunc : StdFunction.values()) {
            var func = getFunction(stdFunc.get().getId());
            EntityUtils.ensureTreeInitialized(func);
            stdFunctions.add(func);
        }
    }

    private void loadColumns(DefContext sysDefContext) {
        for (Column col : ColumnKind.columns()) {
            columns.add(get(Column.class, sysDefContext.getInstance(col).getId()));
        }
    }

    @Override
    public void onInstanceInitialized(Instance instance) {
        if(state == State.INITIALIZING)
            super.onInstanceInitialized(instance);
    }

    private void recordEntityIds() {
        for (Object entity : models()) {
            var inst = getInstance(entity);
            if(inst.tryGetId() != null)
                entity2Id.put(entity, inst.getId());
        }
    }

    public void postInitialization() {
        replaceInstanceContext();
        addMappings();
        replaceEnumConstants();
    }

    private void replaceInstanceContext() {
        var instCtx = getInstanceContext().createSame(getAppId(), this);
        setInstanceContext(instCtx);
        instCtx.addListener(this);
    }

    private void addMappings() {
        var instCtx = getInstanceContext();
        var entities = new ArrayList<>();
        forEachDef(def -> {
            if(def instanceof TypeVariableDef )
                return;
            entities.add(def.getTypeDef());
        });
        entities.addAll(stdFunctions);
        entities.addAll(extraKlasses);
        entities.addAll(columns);
        for (var entity : entities) {
            EntityUtils.forEachDescendant(entity, e -> {
                var id = entity2Id.get(e);
                if(id != null) {
                    var inst = instCtx.get(id);
                    addMapping(e, inst);
                }
            });
        }
    }

    private void replaceEnumConstants() {
        var instCtx = getInstanceContext();
        forEachDef(def -> {
            if(def instanceof EnumDef<?> enumDef) {
                for (EnumConstantDef<?> enumConstantDef : enumDef.getEnumConstantDefs()) {
                    var instance = (ClassInstance) instCtx.get(enumConstantDef.getInstance().getId());
                    enumConstantDef.setEnumConstant(instance);
                    addMapping(enumConstantDef.getValue(), instance);
                }
            }
        });
    }

    private void forEachDef(Consumer<ModelDef<?>> action) {
        javaType2Def.values().forEach(action);
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
        var typeDef = (TypeDef) getTypeDef(prototype.getTypeDef().getId());
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
        initPojoDef(def, prototype);
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
        return new DirectDef<>(prototype.getJavaClass(), (TypeDef) getTypeDef(prototype.getTypeDef().getId()));
    }

    private <T> EntityDef<T> createEntityDef(EntityDef<T> prototype) {
        var klass = getKlass(prototype.getKlass().getId());
       var def = new EntityDef<>(prototype.getJavaClass(), prototype.getJavaType(), getSuperDef(prototype), klass, this);
       initPojoDef(def, prototype);
       return def;
    }

    private <T> ValueDef<T> createValueDef(ValueDef<T> prototype) {
        var klass = getKlass(prototype.getKlass().getId());
        var def = new ValueDef<>(prototype.getJavaClass(), prototype.getEntityType(), getSuperDef(prototype), klass, this);
        initPojoDef(def, prototype);
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

    private void initPojoDef(PojoDef<?> pojoDef, PojoDef<?> prototype) {
        javaType2Def.put(pojoDef.getJavaType(), pojoDef);
        initFieldDefs(pojoDef, prototype);
        initIndexDefs(pojoDef, prototype);
    }

    @Nullable
    @Override
    public ModelDef<?> getDefIfPresent(Type javaType) {
        return javaType2Def.get(javaType);
    }

    private void initFieldDefs(PojoDef<?> pojoDef, PojoDef<?> prototype) {
        for (var prototypeField : prototype.getKlass().getFields()) {
            var field = pojoDef.getKlass().getSelfField(f -> f.getName().equals(prototypeField.getName()));
            var javaField = ReflectionUtils.getField(pojoDef.getJavaClass(), field.getName());
            if (javaField.getType() == Class.class)
                new ClassFieldDef(pojoDef, field, javaField, this);
            else if(Value.class.isAssignableFrom(javaField.getType()))
                new InstanceFieldDef(javaField, field, pojoDef);
            else
                new FieldDef(field, field.isNullable(), javaField, pojoDef, null);
        }
    }

    private void initIndexDefs(PojoDef<?> pojoDef, PojoDef<?> prototype) {
        var klass = pojoDef.getKlass();
        for (IndexConstraintDef indexDef : prototype.getIndexConstraintDefList()) {
            new IndexConstraintDef(
                    Objects.requireNonNull(klass.findIndex(idx -> idx.idEquals(indexDef.getIndexConstraint().getId()))),
                    indexDef.getIndexDefField(),
                    pojoDef
            );
        }
    }

    @Override
    public Class<?> getJavaClassByTag(int tag) {
        return typeTag2Def.get(tag).getEntityClass();
    }


    @Override
    public void close() {
        super.close();
        SystemConfig.clearLocal();
    }
}
