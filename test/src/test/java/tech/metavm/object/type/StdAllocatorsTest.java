package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.object.instance.core.DefaultPhysicalId;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TypePhysicalId;
import tech.metavm.util.ParameterizedTypeImpl;
import tech.metavm.util.ReflectionUtils;

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
        java.lang.reflect.Field typeNameField = ReflectionUtils.getField(Type.class, "name");

        var class2ids = allocators.allocate(
                Map.of(ClassType.class, 1, Field.class, 1)
        );

        
        
        allocators.putId(Field.class, DefaultPhysicalId.of(class2ids.get(ClassType.class).get(0), 0L, TypePhysicalId.ofClass(1L, 0L)));
        allocators.putId(typeNameField, DefaultPhysicalId.of(class2ids.get(Field.class).get(0), 0L, TypePhysicalId.ofClass(1L, 0L)));

        var fieldClassId = allocators.getId(Field.class);
        LOGGER.info("Field id: " + fieldClassId);

        var typeNameFieldId = allocators.getId(typeNameField);
        LOGGER.info("Type.name id: " + typeNameFieldId);
    }

    public void testGetTypeId() {
        java.lang.reflect.Field typeNameReflectField = ReflectionUtils.getField(Type.class, "name");

        var class2ids = allocators.allocate(
                Map.of(ClassType.class, 3, Field.class, 1, TypeCategory.class, 1)
        );
        allocators.putId(ClassType.class, DefaultPhysicalId.of(class2ids.get(ClassType.class).get(0), 0L, TypePhysicalId.ofClass(1L, 0L)));
        allocators.putId(Field.class, DefaultPhysicalId.of(class2ids.get(ClassType.class).get(1), 0L, TypePhysicalId.ofClass(1L, 0L)));
        allocators.putId(TypeCategory.class, DefaultPhysicalId.of(class2ids.get(ClassType.class).get(2), 0L, TypePhysicalId.ofClass(1L, 0L)));
        allocators.putId(typeNameReflectField, DefaultPhysicalId.of(class2ids.get(Field.class).get(0), 0L, TypePhysicalId.ofClass(1L, 0L)));
        allocators.putId(TypeCategory.CLASS, DefaultPhysicalId.of(class2ids.get(TypeCategory.class).get(0), 0L, TypePhysicalId.ofClass(1L, 0L)));

        var typeClassId = allocators.getId(ClassType.class);
        var fieldClassId = allocators.getId(Field.class);
        Assert.assertEquals(typeClassId.getPhysicalId(), allocators.getTypeId(fieldClassId).id());

        var typeNameFieldId = allocators.getId(typeNameReflectField);
        Assert.assertEquals(fieldClassId.getPhysicalId(), allocators.getTypeId(typeNameFieldId).id());

        var typeCategoryClassId = allocators.getId(TypeCategory.class);
        Assert.assertEquals(typeClassId.getPhysicalId(), allocators.getTypeId(typeCategoryClassId).id());

        var enumConstantId = allocators.getId(TypeCategory.CLASS);
        Assert.assertEquals(typeCategoryClassId.getPhysicalId(), allocators.getTypeId(enumConstantId).id());

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