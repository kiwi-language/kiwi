package tech.metavm.entity;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.mocks.*;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Index;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PojoDefTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(PojoDefTest.class);

    private MockDefMap defMap;
    private MockModelInstanceMap modelInstanceMap;

    @Override
    protected void setUp() {
        modelInstanceMap = new MockModelInstanceMap(MockDefMap::new);
        defMap = (MockDefMap) modelInstanceMap.getDefMap();
    }

    public void testDefParsing() {
        EntityDef<Foo> fooDef = defMap.getEntityDef(Foo.class);
        Assert.assertNotNull(fooDef);
    }

    public void testConversion() {
        EntityDef<Foo> fooDef = defMap.getEntityDef(Foo.class);
        Foo foo = new Foo("foo001", new Bar("bar001"));
        ClassInstance instance = fooDef.createInstance(foo, modelInstanceMap);
        Foo recoveredFoo = fooDef.createModel(instance, modelInstanceMap);
        Assert.assertFalse(EntityUtils.isPojoDifferent(foo, recoveredFoo));
    }

    public void testUniqueConstraint() {
        EntityDef<Foo> fooDef = defMap.getEntityDef(Foo.class);
        ClassType fooType = fooDef.getType();
        IndexConstraintDef nameConstraintDef = fooDef.getIndexConstraintDef(Foo.IDX_NAME);
        Assert.assertNotNull(nameConstraintDef);
        Index constraint = nameConstraintDef.getIndexConstraint();
        Assert.assertEquals(
                List.of(fooType.getFieldByName("名称")),
                constraint.getTypeFields()
        );
    }

    public void testInheritance() {
        Human model = new Human(30, 160, "工程师");
        model.initId(100L);
        EntityDef<Human> def = defMap.getEntityDef(Human.class);
        Instance instance = def.createInstance(model, modelInstanceMap);
        Human recoveredModel = def.createModelHelper(instance, modelInstanceMap);
        MatcherAssert.assertThat(recoveredModel, PojoMatcher.of(model));
    }

    public void testPolymorphism() {
        LivingBeing model = new LivingBeing(30L);
        model.addOffspring(new Animal(3L, 10L));
        model.setExtraInfo("This guy is a genius");
        PojoDef<LivingBeing> def = defMap.getEntityDef(LivingBeing.class);
        FieldDef fieldDef = (FieldDef) def.getFieldDef(ReflectUtils.getField(LivingBeing.class, "offsprings"));
        tech.metavm.object.meta.Type fieldType = fieldDef.getTargetDef().getType();
        Assert.assertTrue(fieldType instanceof ArrayType);
        ArrayType arrayType = (ArrayType) fieldType;
        Assert.assertEquals(defMap.getType(LivingBeing.class), arrayType.getElementType());

        ClassInstance instance = def.createInstance(model, modelInstanceMap);
        LivingBeing recoveredModel = def.createModel(instance, modelInstanceMap);
        TestUtils.logJSON(LOGGER, recoveredModel);
        MatcherAssert.assertThat(recoveredModel, PojoMatcher.of(model));
    }

    public void test_polymorphism_for_java_list() {
        LivingBeing model = new LivingBeing(30L);
        model.addAncestor(new Animal(58, 140L));
        PojoDef<LivingBeing> def = defMap.getEntityDef(LivingBeing.class);
        FieldDef fieldDef = (FieldDef) def.getFieldDef(ReflectUtils.getField(LivingBeing.class, "ancestors"));
        tech.metavm.object.meta.Type fieldType = fieldDef.getTargetDef().getType();
        Assert.assertTrue(fieldType instanceof ArrayType);
        ArrayType arrayType = (ArrayType) fieldType;
        Assert.assertEquals(defMap.getType(LivingBeing.class), arrayType.getElementType());

        ClassInstance instance = def.createInstance(model, modelInstanceMap);
        LivingBeing recoveredModel = def.createModel(instance, modelInstanceMap);
        TestUtils.logJSON(LOGGER, recoveredModel);
        MatcherAssert.assertThat(recoveredModel, PojoMatcher.of(model));
    }

    public void test_nullable_field_type() {
        PojoDef<Foo> def = defMap.getEntityDef(Foo.class);

        tech.metavm.object.meta.ClassType fooType = def.getType();
        Field fooQuxField = fooType.getFieldByJavaField(ReflectUtils.getField(Foo.class, "qux"));
        LOGGER.info(fooQuxField.getType().getName());
    }

    public static class MockDefMap implements DefMap {

        private final Map<Type, ModelDef<?,?>> class2def = new HashMap<>();
        private final ModelInstanceMap modelInstanceMap;
        private final ObjectTypeDef<Object> objectDef;
        private final ValueDef<Enum<?>> enumDef;
        private final Map<tech.metavm.object.meta.Type, tech.metavm.object.meta.Type> typeInternMap = new HashMap<>();

        public MockDefMap(ModelInstanceMap modelInstanceMap) {
            this.modelInstanceMap = modelInstanceMap;
            StandardDefBuilder standardDefBuilder = new StandardDefBuilder(this);
            objectDef = standardDefBuilder.getObjectDef();
            enumDef = standardDefBuilder.getEnumDef();
        }

        @Override
        public ModelDef<?,?> getDef(Type type) {
            type = ReflectUtils.getBoxedType(type);
            if(class2def.containsKey(type)) {
                return class2def.get(type);
            }
            ModelDef<?,?> def = parseType(type);
            class2def.put(type, def);
            return def;
        }

        @Override
        public boolean containsDef(Type javaType) {
            return class2def.containsKey(javaType);
        }

        @Override
        public tech.metavm.object.meta.Type internType(tech.metavm.object.meta.Type type) {
            return typeInternMap.computeIfAbsent(type, t -> type);
        }

        @Override
        public ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type) {
            return NncUtils.find(
                    class2def.values(), def -> def.getType() == type
            );
        }

        @Override
        public void preAddDef(ModelDef<?, ?> def) {
            addDef(def);
        }

        private ModelDef<?,?> parseType(Type genericType) {
            DefParser<?,?,?> parser = getParser(genericType);
            for (Type dependencyType : parser.getDependencyTypes()) {
                getDef(dependencyType);
            }
            ModelDef<?,?> def;
            if((def = class2def.get(genericType)) != null) {
                return def;
            }
            def = parser.create();
            addDef(def);
            parser.initialize();
            return def;
        }

        private DefParser<?,?,?> getParser(Type genericType) {
            Class<?> rawClass = ReflectUtils.getRawClass(genericType);
            if(!RuntimeGeneric.class.isAssignableFrom(rawClass)) {
                genericType = rawClass;
            }
            TypeCategory typeCategory = ValueUtil.getTypeCategory(genericType);
/*            if(rawClass == Table.class) {
                if (genericType instanceof ParameterizedType pType) {
                    ModelDef<?,?> elementDef = getDef(pType.getActualTypeArguments()[0]);
                    return new TableDef<>(
                            elementDef,
                            pType,
                            TypeUtil.getArrayType(elementDef.getType())
                    );
                }
                else {
                    throw new InternalException("Raw TableDef should have been defined by StandardDefBuilder");
                }
            }*/
            if(Table.class.isAssignableFrom(rawClass)) {
                Class<? extends Table<?>> collectionClass = rawClass.asSubclass(
                        new TypeReference<Table<?>>(){}.getType()
                );
                if (genericType instanceof ParameterizedType pType) {
                    return new CollectionParser<>(
                            collectionClass,
                            genericType,
                            this
                    );
                }
                else {
                    return new CollectionParser<>(
                            collectionClass,
                            collectionClass,
                            this
                    );
                }
            }
            else {
                if (typeCategory.isEnum()) {
                    Class<? extends Enum<?>> enumType = rawClass.asSubclass(new TypeReference<Enum<?>>() {
                    }.getType());
                    return new EnumParser<>(enumType, enumDef, this);
                }
                if (typeCategory.isEntity()) {
                    return new EntityParser<>(
                            rawClass.asSubclass(Entity.class),
                            genericType,
                            this
                    );
                }
                if (typeCategory.isValue()) {
                    if(Record.class.isAssignableFrom(rawClass)) {
                        return new RecordParser<>(
                                rawClass.asSubclass(Record.class), genericType, this
                        );
                    }
                    else {
                        return new ValueParser<>(
                                rawClass,
                                genericType,
                                this
                        );
                    }
                }
            }
            throw new InternalException("Can not parse definition for type: " + genericType);
        }


        @Override
        public void addDef(ModelDef<?,?> def) {
            class2def.put(def.getJavaClass(), def);
        }

        @SuppressWarnings("unused")
        public ModelInstanceMap getModelMap() {
            return modelInstanceMap;
        }
    }

}

