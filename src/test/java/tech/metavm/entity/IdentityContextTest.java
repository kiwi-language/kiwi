package tech.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.mocks.Foo;
import tech.metavm.object.type.*;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IdentityContextTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(IdentityContextTest.class);

    public void test() {

        Map<Type, java.lang.reflect.Type>  type2javaType = new HashMap<>();

        IdentityContext context = new IdentityContext( t-> true, type2javaType::get);

        PrimitiveType stringType = new PrimitiveType(PrimitiveKind.STRING);
        ClassType fooType = ClassBuilder.newBuilder("Foo", null).build();
        type2javaType.put(stringType, String.class);
        type2javaType.put(fooType, Foo.class);

        Field fooNameField = FieldBuilder.newBuilder(
                ReflectUtils.getMetaFieldName(Foo.class, "name"),
                null, fooType, stringType)
                .build();
        Index uniqueConstraint =
                new Index(fooType, List.of(fooNameField), Foo.IDX_NAME.isUnique(), "name is required");
        uniqueConstraint.setIndexDef(Foo.IDX_NAME);

        context.getIdentityMap(fooType);

        Map<Object, ModelIdentity> model2modelId = context.getIdentityMap();

        context.getIdentityMap().forEach((model, modelId) -> LOGGER.info(model + " -> " + modelId));

        List<Identifiable> models = NncUtils.filterByType(
                ReflectUtils.getReachableObjects(List.of(fooType), o -> true, true),
                Identifiable.class
        );

        for (Identifiable model : models) {
            ModelIdentity modelId = model2modelId.get(model);
            Assert.assertNotNull("ModelId is not found for model '" + model + "'", modelId);
            Assert.assertEquals(ReflectUtils.getType(model), modelId.type());
        }

        Set<String> modelIdCodes = NncUtils.mapUnique(model2modelId.values(), ModelIdentity::name);
        Assert.assertEquals(model2modelId.size(), modelIdCodes.size());
    }

}