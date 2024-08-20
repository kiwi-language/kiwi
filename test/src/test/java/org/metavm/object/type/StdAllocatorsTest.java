package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.ModelIdentity;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.ParameterizedTypeImpl;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StdAllocatorsTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(StdAllocatorTest.class);

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

        allocators.putId(Field.class, PhysicalId.of(class2ids.get(Klass.class).get(0), 0L, TestUtils.mockClassType()));
        allocators.putId(klassNameField, PhysicalId.of(class2ids.get(Field.class).get(0), 0L, TestUtils.mockClassType()));

        var fieldClassId = allocators.getId(Field.class);
        logger.info("Field id: " + fieldClassId);

        var typeNameFieldId = allocators.getId(klassNameField);
        logger.info("Type.name id: " + typeNameFieldId);
    }

    public void testGetTypeId() {
        java.lang.reflect.Field klassNameReflectField = ReflectionUtils.getField(Klass.class, "name");

        var class2ids = allocators.allocate(
                Map.of(Klass.class, 3, Field.class, 1, TypeCategory.class, 1)
        );
        allocators.putId(Klass.class, PhysicalId.of(class2ids.get(Klass.class).get(0), 0L, TestUtils.mockClassType()));
        allocators.putId(Field.class, PhysicalId.of(class2ids.get(Klass.class).get(1), 0L, TestUtils.mockClassType()));
        allocators.putId(TypeCategory.class, PhysicalId.of(class2ids.get(Klass.class).get(2), 0L, TestUtils.mockClassType()));
        allocators.putId(klassNameReflectField, PhysicalId.of(class2ids.get(Field.class).get(0), 0L, TestUtils.mockClassType()));
        allocators.putId(TypeCategory.CLASS, PhysicalId.of(class2ids.get(TypeCategory.class).get(0), 0L, TestUtils.mockClassType()));

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
                        Klass.class, 1,
                        ParameterizedTypeImpl.create(ReadWriteArray.class, Klass.class), 1
                )
        );
        allocators.putId(
                new ModelIdentity(
                        Type.class,
                        "Klass1",
                        false
                )
                , new PhysicalId(false, 1L ,0L));
        allocators.putId(
                new ModelIdentity(
                ParameterizedTypeImpl.create(ReadWriteArray.class, Type.class),
                        "array",
                        false
                ),
                new PhysicalId(false, 2, 0L));
        allocators.save();
        Assert.assertEquals(
                Set.of("/id/" + Type.class.getName() +".properties",
                        "/id/" + ParameterizedTypeImpl.create(ReadWriteArray.class, Type.class).getTypeName() + ".properties"),
                new HashSet<>(allocatorStore.getFileNames())
        );
        Assert.assertEquals(10002, allocatorStore.getNextId());
    }

}