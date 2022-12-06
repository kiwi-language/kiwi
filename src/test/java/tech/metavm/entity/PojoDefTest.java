package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.*;

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
        new StandardDefBuilder().initRootTypes(defMap);
    }

    public void testDefParsing() {
        EntityDef<Foo> fooDef = defMap.getEntityDef(Foo.class);
        Assert.assertNotNull(fooDef);
        TestUtils.logJSON(LOGGER, "EntityDef<Foo>", fooDef);
    }

    public void testConversion() {
        EntityDef<Foo> fooDef = defMap.getEntityDef(Foo.class);
        Foo foo = new Foo("foo001", new Bar("bar001"));
        Instance instance = fooDef.createInstance(foo, modelInstanceMap);
        Foo recoveredFoo = fooDef.createModel(instance, modelInstanceMap);
        Assert.assertFalse(EntityUtils.isPojoDifferent(foo, recoveredFoo));
    }

    public void testGetEntityMapping() {
        EntityDef<Foo> fooDef = defMap.getEntityDef(Foo.class);
        Map<Object, Identifiable> mapping = fooDef.getEntityMapping();
        tech.metavm.object.meta.Type fooType = (tech.metavm.object.meta.Type) mapping.get(Foo.class);

        int mappingSize = 3 + ReflectUtils.getDeclaredPersistentFields(Foo.class).size()
                + ReflectUtils.getIndexDefFields(Foo.class).size();
        Assert.assertEquals(mappingSize, mapping.size());

        Assert.assertEquals("傻", fooType.getName());
        Assert.assertEquals(TypeCategory.CLASS, fooType.getCategory());

        Field nameField = (Field) mapping.get(ReflectUtils.getField(Foo.class, "name"));
        Assert.assertEquals("名称", nameField.getName());
        Assert.assertEquals(StandardTypes.STRING, nameField.getType());

        Field barField = (Field) mapping.get(ReflectUtils.getField(Foo.class, "bar"));
        Assert.assertEquals("巴", barField.getName());
        Assert.assertEquals("巴", barField.getType().getName());
    }

    public void testUniqueConstraint() {
        EntityDef<Foo> fooDef = defMap.getEntityDef(Foo.class);
        tech.metavm.object.meta.Type fooType = fooDef.getType();
        UniqueConstraintDef nameConstraintDef = fooDef.getUniqueConstraintDef(Foo.IDX_NAME);
        Assert.assertNotNull(nameConstraintDef);
        UniqueConstraintRT constraint = nameConstraintDef.getUniqueConstraint();
        Assert.assertEquals(
                List.of(fooType.getFieldByName("名称")),
                constraint.getFields()
        );
    }

    public static class MockDefMap implements DefMap {

        private final Map<Type, ModelDef<?,?>> class2def = new HashMap<>();
        private final ModelInstanceMap modelInstanceMap;

        public MockDefMap(ModelInstanceMap modelInstanceMap) {
            this.modelInstanceMap = modelInstanceMap;
        }

        @Override
        public ModelDef<?,?> getDef(Type type) {
            if(class2def.containsKey(type)) {
                return class2def.get(type);
            }
            ModelDef<?,?> def = parseDef(type);
            class2def.put(type, def);
            return def;
        }

        @Override
        public ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type) {
            return NncUtils.find(
                    class2def.values(), def -> def.getType() == type
            );
        }

        private ModelDef<?,?> parseDef(Type type) {
            if(type instanceof Class<?> klass) {
                if(Entity.class.isAssignableFrom(klass)) {
                    return EntityParser.parse(klass.asSubclass(Entity.class), o -> null, this, modelInstanceMap);
                }
                else if(Record.class.isAssignableFrom(klass)) {
                    return RecordParser.parse(klass.asSubclass(Record.class), o -> null, this, modelInstanceMap);
                }
                else if(klass.isAnnotationPresent(ValueType.class)) {
                    return ValueParser.parse(klass, o -> null, this, modelInstanceMap);
                }
            }
            throw new InternalException("Not an entity type: " + type);
        }

        @Override
        public void addDef(ModelDef<?,?> def) {
            class2def.put(def.getModelType(), def);
        }

        public ModelInstanceMap getModelMap() {
            return modelInstanceMap;
        }
    }

}

