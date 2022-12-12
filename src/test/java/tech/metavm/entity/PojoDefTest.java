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
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
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

    public void testGetEntityMapping() {
        EntityDef<Foo> fooDef = defMap.getEntityDef(Foo.class);
        Map<Object, Identifiable> mapping = fooDef.getEntityMapping();
        ClassType fooType = (ClassType) mapping.get(Foo.class);

        int mappingSize = 4 + ReflectUtils.getDeclaredPersistentFields(Foo.class).size()
                + ReflectUtils.getIndexDefFields(Foo.class).size();
        Assert.assertEquals(mappingSize, mapping.size());

        Assert.assertEquals("傻", fooType.getName());
        Assert.assertEquals(TypeCategory.CLASS, fooType.getCategory());

        Field nameField = (Field) mapping.get(ReflectUtils.getField(Foo.class, "name"));
        Assert.assertEquals("名称", nameField.getName());
        Assert.assertEquals(defMap.getType(String.class), nameField.getType());

        Field barField = (Field) mapping.get(ReflectUtils.getField(Foo.class, "bar"));
        Assert.assertEquals("巴", barField.getName());
        Assert.assertEquals("巴", barField.getType().getName());
    }

    public void testUniqueConstraint() {
        EntityDef<Foo> fooDef = defMap.getEntityDef(Foo.class);
        ClassType fooType = fooDef.getType();
        UniqueConstraintDef nameConstraintDef = fooDef.getUniqueConstraintDef(Foo.IDX_NAME);
        Assert.assertNotNull(nameConstraintDef);
        UniqueConstraintRT constraint = nameConstraintDef.getUniqueConstraint();
        Assert.assertEquals(
                List.of(fooType.getFieldByName("名称")),
                constraint.getFields()
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
        FieldDef fieldDef = def.getFieldDef(ReflectUtils.getField(LivingBeing.class, "offsprings"));
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
        FieldDef fieldDef = def.getFieldDef(ReflectUtils.getField(LivingBeing.class, "ancestors"));
        tech.metavm.object.meta.Type fieldType = fieldDef.getTargetDef().getType();
        Assert.assertTrue(fieldType instanceof ArrayType);
        ArrayType arrayType = (ArrayType) fieldType;
        Assert.assertEquals(defMap.getType(Object.class), arrayType.getElementType());

        ClassInstance instance = def.createInstance(model, modelInstanceMap);
        LivingBeing recoveredModel = def.createModel(instance, modelInstanceMap);
        TestUtils.logJSON(LOGGER, recoveredModel);
        MatcherAssert.assertThat(recoveredModel, PojoMatcher.of(model));
    }

    public static class MockDefMap implements DefMap {

        private final Map<Type, ModelDef<?,?>> class2def = new HashMap<>();
        private final ModelInstanceMap modelInstanceMap;
        private final AnyTypeDef<Object> objectDef;
        private final ValueDef<Enum<?>> enumDef;

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
        public ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type) {
            return NncUtils.find(
                    class2def.values(), def -> def.getType() == type
            );
        }

        private ModelDef<?,?> parseType(Type genericType) {
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
            if(Collection.class.isAssignableFrom(rawClass)) {
                Class<? extends Collection<?>> collectionClass = rawClass.asSubclass(
                        new TypeReference<Collection<?>>(){}.getType()
                );
                if (genericType instanceof ParameterizedType pType) {
                    ModelDef<?,?> elementDef = getDef(pType.getActualTypeArguments()[0]);
                    return CollectionDef.createHelper(
                            collectionClass,
                            genericType,
                            elementDef,
                            TypeUtil.getArrayType(elementDef.getType())
                    );
                }
                else {
                    return CollectionDef.createHelper(
                            collectionClass,
                            collectionClass,
                            objectDef,
                            TypeUtil.getArrayType(objectDef.getType())
                    );
                }
            }
            else {
                if (typeCategory.isEnum()) {
                    Class<? extends Enum<?>> enumType = rawClass.asSubclass(new TypeReference<Enum<?>>() {
                    }.getType());
                    return EnumParser.parse(
                            enumType,
                            enumDef,
                            this
                    );
                }
                if (typeCategory.isEntity()) {
                    return EntityParser.parse(
                            rawClass.asSubclass(Entity.class),
                            genericType,
                            this
                    );
                }
                if (typeCategory.isValue()) {
                    if(Record.class.isAssignableFrom(rawClass)) {
                        return RecordParser.parse(
                                rawClass.asSubclass(Record.class), genericType, this
                        );
                    }
                    else {
                        return ValueParser.parse(
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

