package tech.metavm.util;

import tech.metavm.entity.*;
import tech.metavm.mocks.Bar;
import tech.metavm.mocks.Baz;
import tech.metavm.mocks.Foo;
import tech.metavm.mocks.Qux;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.*;

import java.util.Date;
import java.util.List;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class MockRegistry {

    private static EntityIdProvider ID_PROVIDER;
    private static MemInstanceContext INSTANCE_CONTEXT;
    private static DefContext DEF_CONTEXT;
    private static ModelInstanceMap MODEL_INSTANCE_MAP;

    private static String getFieldName(Class<?> javaType, String javaFieldName) {
        java.lang.reflect.Field javaField = ReflectUtils.getField(javaType, javaFieldName);
        return ReflectUtils.getMetaFieldName(javaField);
    }

    public static void setUp(EntityIdProvider idProvider) {
        NncUtils.requireNonNull(idProvider, "idProvider required");
        ID_PROVIDER = idProvider;
        INSTANCE_CONTEXT = new MemInstanceContext(
                TENANT_ID, idProvider, new MemInstanceStore(), null
        );
        DEF_CONTEXT = new DefContext(o -> null, INSTANCE_CONTEXT);
        MODEL_INSTANCE_MAP = new MockModelInstanceMap(DEF_CONTEXT);
        ReflectUtils.getModelClasses().forEach(DEF_CONTEXT::getDef);
        ModelDefRegistry.setDefContext(DEF_CONTEXT);
        initIds();
    }

    public static void initIds() {
        DEF_CONTEXT.initIds();
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
        return getEntityDef(Baz.class).createInstance(baz, MODEL_INSTANCE_MAP);
    }

    public static ClassInstance getNewFooInstance(String fooName, String barCode) {
        return getFooInstance(fooName, barCode, false);
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
        ClassInstance instance = getEntityDef(Foo.class).createInstance(foo, MODEL_INSTANCE_MAP);
        if(initId) {
            for (Instance inst : InstanceUtils.getAllNonValueInstances(List.of(instance))) {
                inst.initId(ID_PROVIDER.allocateOne(TENANT_ID, inst.getType()));
            }
        }
        return instance;
    }

    public static Foo getFoo() {
        Foo foo = new Foo(
                "Big Foo",
                new Bar("Bar001")
        );

        foo.setQux(new Qux(100));
        Baz baz1 = new Baz();
        baz1.setBars(List.of(new Bar("Bar002")));
        Baz baz2 = new Baz();
        foo.setBazList(List.of(baz1, baz2));
        return foo;
    }

    private static void initIdRecursively(Instance instance, EntityIdProvider idProvider) {
        initIdRecursively(instance, idProvider, new IdentitySet<>());
    }

    private static void initIdRecursively(Instance instance, EntityIdProvider idProvider, IdentitySet<Instance> visited) {
        if(visited.contains(instance)) {
            return;
        }
        visited.add(instance);
        if(instance.getId() == null && !instance.getType().isValue()) {
            instance.initId(idProvider.allocateOne(TENANT_ID, instance.getType()));
        }
        for (Instance refInstance : instance.getRefInstances()) {
            initIdRecursively(refInstance, idProvider, visited);
        }
    }

    public static <T> ModelDef<T,?> getDef(Class<T> javaType) {
        ModelDef<T,?> def = DEF_CONTEXT.getDef(javaType);
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

    public static Instance getInstance(Object model, ModelInstanceMap modelInstanceMap) {
        NncUtils.requireNonNull(model);
        return getDef(model.getClass()).createInstanceHelper(model, modelInstanceMap);
    }

    public static Type getType(Class<?> javaType) {
        return DEF_CONTEXT.getType(javaType);
    }

    public static ClassType getClassType(Class<?> javaType) {
        return (ClassType) getType(javaType);
    }
    
    public static Field getField(Class<?> javaType, String javaFieldName) {
        return DEF_CONTEXT.getField(javaType, javaFieldName);
    }

    public static Type getType(long id) {
        return DEF_CONTEXT.getType(id);
    }

    private static void initTypeAndFieldIds(Type type, EntityIdProvider idProvider, IdentitySet<Type> visited) {
        if(visited.contains(type)) {
            return;
        }
        visited.add(type);

        ClassType typeType = getClassType(ClassType.class);
        ClassType fieldType = getClassType(Field.class);

        if(type.getId() == null) {
            type.initId(idProvider.allocateOne(TENANT_ID, typeType));
        }
        if(type instanceof ClassType t) {
            if (t.getSuperType() != null) {
                initTypeAndFieldIds(t.getSuperType(), idProvider, visited);
            }
            for (Field field : t.getDeclaredFields()) {
                if (field.getId() == null) {
                    field.initId(idProvider.allocateOne(TENANT_ID, fieldType));
                }
                initTypeAndFieldIds(
                        field.getType(),
                        idProvider,
                        visited
                );
            }
        }
    }

    public static UniqueConstraintRT getUniqueConstraint(IndexDef<?> def) {
        EntityDef<?> entityDef = (EntityDef<?>) getDef(def.getType());
        initIds();
        return entityDef.getUniqueConstraintDef(def).getUniqueConstraint();
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


    public static DefContext getDefContext() {
        return DEF_CONTEXT;
    }

    public static MemInstanceContext getInstanceContext() {
        return INSTANCE_CONTEXT;
    }

}
