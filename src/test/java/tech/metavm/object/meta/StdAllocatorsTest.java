package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.util.ParameterizedTypeImpl;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.util.ReflectUtils;

import java.util.HashSet;
import java.util.List;
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
        java.lang.reflect.Field typeNameField = ReflectUtils.getField(Type.class, "name");

        Map<java.lang.reflect.Type, List<Long>> class2ids = allocators.allocate(
                Map.of(ClassType.class, 1, Field.class, 1)
        );

        allocators.putId(Field.class, class2ids.get(ClassType.class).get(0));
        allocators.putId(typeNameField, class2ids.get(Field.class).get(0));

        long fieldClassId = allocators.getId(Field.class);
        LOGGER.info("Field id: " + fieldClassId);

        long typeNameFieldId = allocators.getId(typeNameField);
        LOGGER.info("Type.name id: " + typeNameFieldId);
    }

    public void testGetTypeId() {
        java.lang.reflect.Field typeNameReflectField = ReflectUtils.getField(Type.class, "name");

        Map<java.lang.reflect.Type, List<Long>> class2ids = allocators.allocate(
                Map.of(ClassType.class, 3, Field.class, 1, TypeCategory.class, 1)
        );
        allocators.putId(ClassType.class, class2ids.get(ClassType.class).get(0));
        allocators.putId(Field.class, class2ids.get(ClassType.class).get(1));
        allocators.putId(TypeCategory.class, class2ids.get(ClassType.class).get(2));
        allocators.putId(typeNameReflectField, class2ids.get(Field.class).get(0));
        allocators.putId(TypeCategory.CLASS, class2ids.get(TypeCategory.class).get(0));

        long typeClassId = allocators.getId(ClassType.class);
        long fieldClassId = allocators.getId(Field.class);
        Assert.assertEquals(typeClassId, allocators.getTypeId(fieldClassId));

        long typeNameFieldId = allocators.getId(typeNameReflectField);
        Assert.assertEquals(fieldClassId, allocators.getTypeId(typeNameFieldId));

        long typeCategoryClassId = allocators.getId(TypeCategory.class);
        Assert.assertEquals(typeClassId, allocators.getTypeId(typeCategoryClassId));

        long enumConstantId = allocators.getId(TypeCategory.CLASS);
        Assert.assertEquals(typeCategoryClassId, allocators.getTypeId(enumConstantId));

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