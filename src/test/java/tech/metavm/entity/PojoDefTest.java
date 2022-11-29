package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class PojoDefTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(PojoDefTest.class);

    private MockDefMap defMap;
    private InstanceMap instanceMap;

    @Override
    protected void setUp() {
        defMap = new MockDefMap();
        instanceMap = new MockInstanceMap(this::getDef);
        new StandardDefBuilder().initRootTypes(defMap);
    }

    private ModelDef<?,?> getDef(Class<?> klass) {
        return defMap.getDef(klass);
    }

    public void testDefParsing() {
        EntityDef<Foo> entityDef = defMap.getEntityDef(Foo.class);
        Assert.assertNotNull(entityDef);
        TestUtils.logJSON(LOGGER, "EntityDef<Foo>", entityDef);
    }

    public void testConvertingToInstance() {
        EntityDef<Foo> entityDef = defMap.getEntityDef(Foo.class);
        Foo foo = new Foo("foo001", new Bar("bar001"));
        Instance fooInst = entityDef.newInstance(foo, instanceMap);
        TestUtils.logJSON(LOGGER, "instance converted from model", fooInst.toDTO());
        Assert.assertNotNull(fooInst);
    }

    public void testConvertingToModel() {
        EntityDef<Foo> entityDef = defMap.getEntityDef(Foo.class);
        Foo foo = new Foo("foo001", new Bar("bar001"));
        Instance fooInst = entityDef.newInstance(foo, instanceMap);
        Foo restoredFoo = entityDef.newModel(fooInst, defMap.getModelMap());
        TestUtils.logJSON(LOGGER, "restored model", restoredFoo);
        Assert.assertNotNull(restoredFoo);
        Assert.assertFalse(EntityUtils.isPojoDifferent(foo, restoredFoo));
    }

    public void testGetEntityMapping() {
        EntityDef<Foo> entityDef = defMap.getEntityDef(Foo.class);
        Map<Object, Entity> mapping = entityDef.getEntityMapping();
        tech.metavm.object.meta.Type fooType = (tech.metavm.object.meta.Type) mapping.get(Foo.class);

        int mappingSize = 1 + Foo.class.getDeclaredFields().length;
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


    private static class MockDefMap implements DefMap {

        private final Map<Type, ModelDef<?,?>> class2def = new HashMap<>();
        private final ModelMap modelMap;

        public MockDefMap() {
            this.modelMap = new ModelMap() {
                @Override
                public <T> T get(Class<T> klass, Instance instance) {
                    ModelDef<?,?> modelDef = getDef(instance.getEntityType());
                    return klass.cast(modelDef.newModelHelper(instance, this));
                }
            };
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

        private ModelDef<?,?> parseDef(Type type) {
            if(type instanceof Class<?> klass) {
                if(Entity.class.isAssignableFrom(klass)) {
                    return EntityParser.parse(klass.asSubclass(Entity.class), o -> null, this, modelMap);
                }
                else if(Record.class.isAssignableFrom(klass)) {
                    return RecordParser.parse(klass.asSubclass(Record.class), o -> null, this, modelMap);
                }
                else if(klass.isAnnotationPresent(ValueType.class)) {
                    return ValueParser.parse(klass, o -> null, this, modelMap);
                }
            }
            throw new InternalException("Not an entity type: " + type);
        }

        @Override
        public void putDef(Type type, ModelDef<?,?> def) {
            class2def.put(type, def);
        }

        public ModelMap getModelMap() {
            return modelMap;
        }
    }

}

