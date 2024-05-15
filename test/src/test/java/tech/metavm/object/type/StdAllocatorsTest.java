package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.object.instance.core.DefaultPhysicalId;
import tech.metavm.util.ParameterizedTypeImpl;
import tech.metavm.util.ReflectionUtils;
import tech.metavm.util.TestUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StdAllocatorsTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(StdAllocatorTest.class);

    private StdAllocators allocators;
    private AllocatorStore allocatorStore;

    @Override
    protected void setUp() {
        allocatorStore = new MemAllocatorStore();
        allocators = new StdAllocators(allocatorStore);
    }

    public void testSmoking() {
        java.lang.reflect.Field klassNameField = ReflectionUtils.getField(Klass.class, "name");
        var class2ids = allocators.allocate(Map.of(Klass.class, 1, Field.class, 1));

        allocators.putId(Field.class, DefaultPhysicalId.ofObject(class2ids.get(Klass.class).get(0), 0L, TestUtils.mockClassTypeKey()));
        allocators.putId(klassNameField, DefaultPhysicalId.ofObject(class2ids.get(Field.class).get(0), 0L, TestUtils.mockClassTypeKey()));

        var fieldClassId = allocators.getId(Field.class);
        LOGGER.info("Field id: " + fieldClassId);

        var typeNameFieldId = allocators.getId(klassNameField);
        LOGGER.info("Type.name id: " + typeNameFieldId);
    }

    public void testGetTypeId() {
        java.lang.reflect.Field klassNameReflectField = ReflectionUtils.getField(Klass.class, "name");

        var class2ids = allocators.allocate(
                Map.of(Klass.class, 3, Field.class, 1, TypeCategory.class, 1)
        );
        allocators.putId(Klass.class, DefaultPhysicalId.ofObject(class2ids.get(Klass.class).get(0), 0L, TestUtils.mockClassTypeKey()));
        allocators.putId(Field.class, DefaultPhysicalId.ofObject(class2ids.get(Klass.class).get(1), 0L, TestUtils.mockClassTypeKey()));
        allocators.putId(TypeCategory.class, DefaultPhysicalId.ofObject(class2ids.get(Klass.class).get(2), 0L, TestUtils.mockClassTypeKey()));
        allocators.putId(klassNameReflectField, DefaultPhysicalId.ofObject(class2ids.get(Field.class).get(0), 0L, TestUtils.mockClassTypeKey()));
        allocators.putId(TypeCategory.CLASS, DefaultPhysicalId.ofObject(class2ids.get(TypeCategory.class).get(0), 0L, TestUtils.mockClassTypeKey()));

        var typeClassId = allocators.getId(Klass.class);
        var fieldClassId = allocators.getId(Field.class);
        Assert.assertEquals(typeClassId.getTreeId(), allocators.getTypeId(fieldClassId).id());

        var typeNameFieldId = allocators.getId(klassNameReflectField);
        Assert.assertEquals(fieldClassId.getTreeId(), allocators.getTypeId(typeNameFieldId).id());

        var typeCategoryClassId = allocators.getId(TypeCategory.class);
        Assert.assertEquals(typeClassId.getTreeId(), allocators.getTypeId(typeCategoryClassId).id());

        var enumConstantId = allocators.getId(TypeCategory.CLASS);
        Assert.assertEquals(typeCategoryClassId.getTreeId(), allocators.getTypeId(enumConstantId).id());

        allocators.save();
    }

    public void testFileNames() {
        allocators.allocate(
                Map.of(
                        Type.class, 1,
                        ParameterizedTypeImpl.create(ReadWriteArray.class, Type.class), 1
                )
        );

        allocators.save();

        Assert.assertEquals(
                Set.of("/id/" + Type.class.getName() +".properties",
                        "/id/" + ParameterizedTypeImpl.create(ReadWriteArray.class, Type.class).getTypeName() + ".properties"),
                new HashSet<>(allocatorStore.getFileNames())
        );
    }

}