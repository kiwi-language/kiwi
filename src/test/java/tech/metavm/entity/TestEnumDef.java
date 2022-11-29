package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.TestUtils;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TestEnumDef extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEnumDef.class);

    private final DefMap defMap = new DefMap() {

        private final Map<Type, ModelDef<?,?>> map = new HashMap<>();

        @Override
        public ModelDef<?, ?> getDef(Type type) {
            return map.get(type);
        }

        @Override
        public void putDef(Type type, ModelDef<?, ?> def) {
            map.put(type, def);
        }
    };

    private final ModelMap modelMap = new ModelMap() {
        @Override
        public <T> T get(Class<T> klass, Instance instance) {
            return null;
        }
    };

    public void test() {
        ValueDef<Object> objectDef = new ValueDef<>(
                "对象", Object.class, null, StandardTypes.OBJECT, defMap
        );

        ValueDef<Enum<?>> enumDef = new ValueDef<>(
                "枚举", new TypeReference<>(){}, objectDef, StandardTypes.ENUM, defMap
        );

        EnumDef<TypeCategory> typeCategoryDef =  EnumParser.parse(
                TypeCategory.class,
                enumDef,
                e -> null,
                modelMap,
                defMap
        );

        Map<Object, Entity> mapping =  typeCategoryDef.getEntityMapping();
        int expectedMappingSize = 1 + TypeCategory.values().length;
        Assert.assertEquals(expectedMappingSize, mapping.size());

        Instance enumInstance = typeCategoryDef.getEnumConstantDefs().get(0).getInstance();

        TestUtils.logJSON(LOGGER, enumInstance.toDTO());
    }

}
