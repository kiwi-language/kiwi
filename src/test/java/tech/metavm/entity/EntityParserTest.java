package tech.metavm.entity;

import junit.framework.TestCase;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.util.Foo;
import tech.metavm.util.InternalException;
import tech.metavm.util.TestUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EntityParserTest extends TestCase {

    private final MockIdAllocator idAllocator = new MockIdAllocator();
    private final ModelMap modelMap = new MockModelMap();
    private final DefMap defMap = new MockDefMap(idAllocator::getId, modelMap);

    @Override
    protected void setUp() {
        new StandardDefBuilder().initRootTypes(o -> null, defMap::putDef, modelMap);
    }

    public void test() {
        EntityDef<Foo> entityDef = EntityParser.parse(
                Foo.class,
                o -> null,
                defMap,
                modelMap
        );
        TestUtils.printJSON(entityDef);
    }

    private static class MockDefMap implements DefMap {

        private final Map<Type, ModelDef<?,?>> class2def = new HashMap<>();
        private final Function<Object,Long> getId;
        private final ModelMap modelMap;

        public MockDefMap(Function<Object,Long> getId, ModelMap modelMap) {
            this.getId = getId;
            this.modelMap = modelMap;
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
            }
            throw new InternalException("Not an entity type: " + type);
        }

        @Override
        public void putDef(Type type, ModelDef<?,?> def) {
            class2def.put(type, def);
        }

    }

}

