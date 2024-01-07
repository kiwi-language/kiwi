package tech.metavm.util;

import tech.metavm.entity.*;
import tech.metavm.event.MockEventQueue;
import tech.metavm.mocks.*;
import tech.metavm.object.instance.CheckConstraintPlugin;
import tech.metavm.object.instance.IndexConstraintPlugin;
import tech.metavm.object.instance.MockInstanceLogService;
import tech.metavm.object.instance.ObjectInstanceMap;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;
import tech.metavm.task.JobSchedulerStatus;
import tech.metavm.task.TaskSignal;

import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static tech.metavm.util.Constants.ROOT_APP_ID;
import static tech.metavm.util.TestConstants.APP_ID;
import static tech.metavm.util.TestConstants.USER_ID;

public class MockRegistry {

    private static EntityIdProvider ID_PROVIDER;
    private static MemInstanceStore INSTANCE_STORE;
    private static InstanceContext INSTANCE_CONTEXT;
    private static DefContext DEF_CONTEXT;
    private static ObjectInstanceMap MODEL_INSTANCE_MAP;
    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static InstanceContextFactory CONTEXT_FACTORY;
    private static EntityContextFactory ENTITY_CONTEXT_FACTORY;

    private static String getFieldName(Class<?> javaType, String javaFieldName) {
        java.lang.reflect.Field javaField = ReflectionUtils.getField(javaType, javaFieldName);
        return EntityUtils.getMetaFieldName(javaField);
    }

    public static void setUp(EntityIdProvider idProvider) {
        setUp(idProvider, new MemInstanceStore());
    }

    public static void setUp(EntityIdProvider idProvider, MemInstanceStore instanceStore) {
        NncUtils.requireNonNull(idProvider, "idProvider required");
        ID_PROVIDER = idProvider;
        var instanceLogService = new MockInstanceLogService();
        CONTEXT_FACTORY = new InstanceContextFactory(instanceStore, new MockEventQueue());
        ENTITY_CONTEXT_FACTORY = new EntityContextFactory(CONTEXT_FACTORY, instanceStore.getIndexEntryMapper());
        ENTITY_CONTEXT_FACTORY.setInstanceLogService(instanceLogService);
        CONTEXT_FACTORY.setIdService(idProvider);
        INSTANCE_STORE = instanceStore;
        var dep = new EntityInstanceContextBridge();
        INSTANCE_CONTEXT = (InstanceContext)
                InstanceContextBuilder.newBuilder(ROOT_APP_ID, instanceStore, idProvider,
                                dep, dep, dep)
                        .plugins(List.of(
                                new CheckConstraintPlugin(),
                                new IndexConstraintPlugin(instanceStore.getIndexEntryMapper(), dep)
                        ))
                        .executor(EXECUTOR)
                        .build();
        java.util.function.Function<Object, Long> getIdFunc;
        if (idProvider instanceof BootIdProvider bootIdProvider) {
            getIdFunc = bootIdProvider::getId;
        } else getIdFunc = o -> null;
        DEF_CONTEXT = new DefContext(getIdFunc, INSTANCE_CONTEXT, new MemColumnStore());
        dep.setEntityContext(DEF_CONTEXT);
        ModelDefRegistry.setDefContext(DEF_CONTEXT);
//        INSTANCE_CONTEXT.setEntityContext(DEF_CONTEXT);
        MODEL_INSTANCE_MAP = DEF_CONTEXT.getObjectInstanceMap();
        EntityUtils.getModelClasses().stream()
                .filter(k -> !ReadonlyArray.class.isAssignableFrom(k))
                .forEach(DEF_CONTEXT::getDef);
        DEF_CONTEXT.finish();
        InstanceContextFactory.setDefContext(DEF_CONTEXT);
        ContextUtil.setAppId(APP_ID);
        ContextUtil.setUserId(USER_ID);
        initJobScheduler();
    }

    private static void initJobScheduler() {
        var dep = new EntityInstanceContextBridge();
        try (var rootContext =
                     EntityContextBuilder.newBuilder(INSTANCE_STORE, EXECUTOR, ID_PROVIDER)
                             .parent(DEF_CONTEXT)
                             .appId(ROOT_APP_ID)
                             .plugins(List.of(
                                     new CheckConstraintPlugin(),
                                     new IndexConstraintPlugin(INSTANCE_STORE.getIndexEntryMapper(), dep)
                             )).build()) {
            dep.setEntityContext(rootContext);
            rootContext.bind(new JobSchedulerStatus());
            rootContext.bind(new TaskSignal(APP_ID));
            rootContext.finish();
        }
    }

    public static void initIds() {
        DEF_CONTEXT.initIds();
    }

    public static NullInstance getNullInstance() {
        return new NullInstance((PrimitiveType) getType(Null.class));
    }

    public static ClassInstance getFooInstance() {
        return getFooInstance("Big Foo", "Bar001", true);
    }

    public static ClassInstance getFooInstance(String fooName, String barCode) {
        return getFooInstance(fooName, barCode, true);
    }

    public static ClassInstance getNewFooInstance() {
        return getFooInstance("Big Foo", "Bar001", false);
    }

    public static ClassInstance getNewBazInstance() {
        Baz baz = new Baz(List.of(
                new Bar("Bar001")
        ));
        return getEntityDef(Baz.class).createInstance(baz, MODEL_INSTANCE_MAP, null);
    }

    public static ClassInstance getNewFooInstance(String fooName, String barCode) {
        return getFooInstance(fooName, barCode, false);
    }

    public static MemInstanceStore getInstanceStore() {
        return INSTANCE_STORE;
    }

    public static ClassInstance getFooInstance(String fooName, String barCode, boolean initId) {
        Foo foo = new Foo(
                fooName,
                new Bar(barCode),
                new Qux(100),
                List.of(
                        new Baz(
                                List.of(new Bar("Bar002"))
                        ),
                        new Baz(
                                List.of(new Bar("Bar003"))
                        )
                )
        );
        ClassInstance instance = getEntityDef(Foo.class).createInstance(foo, MODEL_INSTANCE_MAP, null);
        if (initId) {
            initInstanceIds(instance);
        }
        return instance;
    }

    public static Coupon getCoupon() {
        Product product = new Product("shoes", 100, 100);
        return new Coupon(0.8, DiscountType.PERCENTAGE, CouponState.UNUSED, product);
    }

    public static ClassInstance getNewCouponInstance() {
        return getCouponInstance(false);
    }

    public static ClassInstance getCouponInstance() {
        return getCouponInstance(true);
    }

    private static ClassInstance getCouponInstance(boolean initId) {
        Coupon coupon = getCoupon();
        ClassInstance instance = getEntityDef(Coupon.class).createInstance(coupon, MODEL_INSTANCE_MAP, null);
        if (initId) {
            initInstanceIds(instance);
        }
        INSTANCE_CONTEXT.replace(instance);
        return instance;
    }

    private static void initInstanceIds(DurableInstance instance) {
        for (var inst : Instances.getAllNonValueInstances(List.of(instance))) {
            if (inst.tryGetPhysicalId() == null) {
                inst.initId(PhysicalId.of(ID_PROVIDER.allocateOne(APP_ID, inst.getType())));
            }
        }
    }

    private static void initModelIds(Entity model) {
        EntityUtils.traverse(
                model,
                m -> m.initId(ID_PROVIDER.allocateOne(APP_ID, getType(m.getEntityType())))
        );
    }

    public static IEntityContext newEntityContext(long appId) {
        return ENTITY_CONTEXT_FACTORY.newContext(appId);
    }

    public static IInstanceContext newContext(long appId) {
        //noinspection resource
        return newEntityContext(appId).getInstanceContext();
    }

    public static Foo getFoo() {
        Foo foo = new Foo(
                "Big Foo",
                new Bar("Bar001")
        );
        foo.setCode("Foo001");

        foo.setQux(new Qux(100));
        Baz baz1 = new Baz();
        baz1.setBars(List.of(new Bar("Bar002")));
        Baz baz2 = new Baz();
        foo.setBazList(List.of(baz1, baz2));
        return foo;
    }

    private static void initIdRecursively(DurableInstance instance, EntityIdProvider idProvider) {
        initIdRecursively(instance, idProvider, new IdentitySet<>());
    }

    private static void initIdRecursively(DurableInstance instance, EntityIdProvider idProvider, IdentitySet<Instance> visited) {
        if (visited.contains(instance)) {
            return;
        }
        visited.add(instance);
        if (instance.tryGetPhysicalId() == null && !instance.getType().isValue()) {
            instance.initId(PhysicalId.of(idProvider.allocateOne(APP_ID, instance.getType())));
        }
        for (var refInstance : instance.getRefInstances()) {
            initIdRecursively(refInstance, idProvider, visited);
        }
    }

    public static <T> ModelDef<T, ?> getDef(Class<T> javaType) {
        ModelDef<T, ?> def = DEF_CONTEXT.getDef(javaType);
        initIds();
        return def;
    }

    public static <T extends Entity> EntityDef<T> getEntityDef(Class<T> javaType) {
        EntityDef<T> def = DEF_CONTEXT.getEntityDef(javaType);
        initIds();
        return def;
    }

    public static Instance getInstance(Object model) {
        return MODEL_INSTANCE_MAP.getInstance(model);
    }

    public static Instance getInstance(Object model, ObjectInstanceMap objectInstanceMap) {
        NncUtils.requireNonNull(model);
        return getDef(model.getClass()).createInstanceHelper(model, objectInstanceMap, null);
    }

    public static Type getType(Class<?> javaClass) {
        javaClass = EntityUtils.getRealType(javaClass);
        return DEF_CONTEXT.getType(javaClass);
    }

    public static Type getType(java.lang.reflect.Type javaType) {
        return DEF_CONTEXT.getType(javaType);
    }

    public static ClassType getClassType(Class<?> javaType) {
        return (ClassType) getType(javaType);
    }

    public static ArrayType getArrayTypeByElementClass(Class<?> elementClass) {
        ParameterizedType parameterizedType = ParameterizedTypeImpl.create(Table.class, elementClass);
        return (ArrayType) getType(parameterizedType);
    }

    public static Field getField(Class<?> javaType, String javaFieldName) {
        return DEF_CONTEXT.getField(javaType, javaFieldName);
    }

    public static Type getType(long id) {
        return DEF_CONTEXT.getType(id);
    }

    private static void initTypeAndFieldIds(Type type, EntityIdProvider idProvider, IdentitySet<Type> visited) {
        if (visited.contains(type)) {
            return;
        }
        visited.add(type);

        ClassType typeType = getClassType(ClassType.class);
        ClassType fieldType = getClassType(Field.class);

        if (type.tryGetId() == null) {
            type.initId(idProvider.allocateOne(APP_ID, typeType));
        }
        if (type instanceof ClassType t) {
            if (t.getSuperClass() != null) {
                initTypeAndFieldIds(t.getSuperClass(), idProvider, visited);
            }
            for (Field field : t.getDeclaredFields()) {
                if (field.tryGetId() == null) {
                    field.initId(idProvider.allocateOne(APP_ID, fieldType));
                }
                initTypeAndFieldIds(
                        field.getType(),
                        idProvider,
                        visited
                );
            }
        }
    }

    public static Index getIndexConstraint(IndexDef<?> def) {
        EntityDef<?> entityDef = (EntityDef<?>) getDef(def.getType());
        initIds();
        return entityDef.getIndexConstraintDef(def).getIndexConstraint();
    }

    public static java.lang.reflect.Type getJavaType(Type type) {
        return DEF_CONTEXT.getJavaType(type);
    }

    public static AnyType getObjectType() {
        return (AnyType) ModelDefRegistry.getType(Object.class);
    }

    public static ClassType getEnumType() {
        return (ClassType) ModelDefRegistry.getType(Enum.class);
    }

    public static ClassType getEntityType() {
        return (ClassType) ModelDefRegistry.getType(Entity.class);
    }

    public static ClassType getRecordType() {
        return (ClassType) ModelDefRegistry.getType(Record.class);
    }

    public static ClassType getIntType() {
        return (ClassType) ModelDefRegistry.getType(Integer.class);
    }

    public static PrimitiveType getBoolType() {
        return (PrimitiveType) getType(Boolean.class);
    }

    public static PrimitiveType getLongType() {
        return (PrimitiveType) getType(Long.class);
    }

    public static PrimitiveType getStringType() {
        return (PrimitiveType) getType(String.class);
    }

    public static PrimitiveType getTimeType() {
        return (PrimitiveType) getType(Date.class);
    }

    public static PrimitiveType getNullType() {
        return (PrimitiveType) getType(Null.class);
    }

    public static PrimitiveType getPasswordType() {
        return (PrimitiveType) getType(Password.class);
    }

    public static PrimitiveType getDoubleType() {
        return (PrimitiveType) getType(Double.class);
    }

    public static ClassType getArrayType() {
        return (ClassType) getType(Table.class);
    }

    public static StringInstance createString(String value) {
        return new StringInstance(value, getStringType());
    }

    public static DefContext getDefContext() {
        return DEF_CONTEXT;
    }

    public static IInstanceContext getInstanceContext() {
        return INSTANCE_CONTEXT;
    }

}
