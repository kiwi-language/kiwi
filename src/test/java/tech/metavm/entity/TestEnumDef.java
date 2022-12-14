package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.TestUtils;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TestEnumDef extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestEnumDef.class);

    private DefMap defMap;
    private StandardDefBuilder standardDefBuilder;

    @Override
    protected void setUp() throws Exception {
        standardDefBuilder = new StandardDefBuilder(defMap = new MockDefMap());
    }

    public void test() {
        ValueDef<Enum<?>> enumDef = new ValueDef<>(
                new TypeReference<Enum<?>>(){}.getType(),
                Enum.class,
                null,
                standardDefBuilder.getEnumType()
                , defMap
        );

        new FieldDef(
                standardDefBuilder.getEnumNameField(),
                false,
                ReflectUtils.getField(Enum.class, "name"),
                enumDef,
                null
        );

        new FieldDef(
                standardDefBuilder.getEnumOrdinalField(),
                false,
                ReflectUtils.getField(Enum.class, "ordinal"),
                enumDef,
                null
        );

        EnumDef<TypeCategory> typeCategoryDef =  EnumParser.parse(
                TypeCategory.class,
                enumDef,
                defMap
        );

        Map<Object, Identifiable> mapping =  typeCategoryDef.getEntityMapping();
        Assert.assertEquals(1, mapping.size());
        Assert.assertEquals(typeCategoryDef.getInstanceMapping().size(), TypeCategory.values().length);

        Instance enumInstance = typeCategoryDef.getEnumConstantDefs().get(0).getInstance();

        TestUtils.logJSON(LOGGER, enumInstance.toDTO());
    }

    private static class MockDefMap implements DefMap {

        private final Map<Type, ModelDef<?,?>> javaType2Def = new HashMap<>();
        private final Map<tech.metavm.object.meta.Type, ModelDef<?,?>> type2Def = new HashMap<>();
        private final Map<tech.metavm.object.meta.Type, tech.metavm.object.meta.Type> internTypeMap = new HashMap<>();

        @Override
        public ModelDef<?, ?> getDef(Type type) {
            return javaType2Def.get(type);
        }

        @Override
        public tech.metavm.object.meta.Type internType(tech.metavm.object.meta.Type type) {
            return internTypeMap.computeIfAbsent(type, t -> type);
        }

        @Override
        public ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type) {
            return type2Def.get(type);
        }

        @Override
        public void preAddDef(ModelDef<?, ?> def) {
            addDef(def);
        }

        @Override
        public void addDef(ModelDef<?, ?> def) {
            javaType2Def.put(def.getJavaClass(), def);
            type2Def.put(def.getType(), def);
        }

    }

}
