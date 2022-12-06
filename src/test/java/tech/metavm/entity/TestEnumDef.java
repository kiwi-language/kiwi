package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.EmptyModelInstanceMap;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelInstanceMap;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.TestUtils;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TestEnumDef extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEnumDef.class);

    private final DefMap defMap = new DefMap() {

        private final Map<Type, ModelDef<?,?>> javaType2Def = new HashMap<>();
        private final Map<tech.metavm.object.meta.Type, ModelDef<?,?>> type2Def = new HashMap<>();

        @Override
        public ModelDef<?, ?> getDef(Type type) {
            return javaType2Def.get(type);
        }

        @Override
        public ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type) {
            return type2Def.get(type);
        }

        @Override
        public void addDef(ModelDef<?, ?> def) {
            javaType2Def.put(def.getModelType(), def);
            type2Def.put(def.getType(), def);
        }
    };

    private final ModelInstanceMap modelInstanceMap = new EmptyModelInstanceMap();

    public void test() {
        ValueDef<Object> objectDef = new ValueDef<>(
                "对象", Object.class, null, StandardTypes.OBJECT, defMap
        );

        ValueDef<Enum<?>> enumDef = new ValueDef<>(
                "枚举", new TypeReference<>(){}, objectDef, StandardTypes.ENUM, defMap
        );

        new FieldDef(
                StandardTypes.ENUM_NAME,
                ReflectUtils.getField(Enum.class, "name"),
                enumDef,
                null
        );

        new FieldDef(
                StandardTypes.ENUM_ORDINAL,
                ReflectUtils.getField(Enum.class, "ordinal"),
                enumDef,
                null
        );

        EnumDef<TypeCategory> typeCategoryDef =  EnumParser.parse(
                TypeCategory.class,
                enumDef,
                e -> null,
                modelInstanceMap,
                defMap
        );

        Map<Object, Identifiable> mapping =  typeCategoryDef.getEntityMapping();
        Assert.assertEquals(1, mapping.size());
        Assert.assertEquals(typeCategoryDef.getInstanceMapping().size(), TypeCategory.values().length);

        Instance enumInstance = typeCategoryDef.getEnumConstantDefs().get(0).getInstance();

        TestUtils.logJSON(LOGGER, enumInstance.toDTO());
    }

}
