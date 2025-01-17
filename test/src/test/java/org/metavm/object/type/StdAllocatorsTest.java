package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.ModelIdentity;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        var ids = allocators.allocate(2);

        allocators.putId(Field.class, PhysicalId.of(ids.getFirst(), 0L));
        allocators.putId(klassNameField, PhysicalId.of(ids.getFirst(), 0L));

        var fieldClassId = allocators.getId(Field.class);
        logger.info("Field id: " + fieldClassId);

        var typeNameFieldId = allocators.getId(klassNameField);
        logger.info("Type.name id: " + typeNameFieldId);
    }

    public void testGetTypeId() {
        java.lang.reflect.Field klassNameReflectField = ReflectionUtils.getField(Klass.class, "name");

        var ids = allocators.allocate(5);
        allocators.putId(Klass.class, PhysicalId.of(ids.getFirst(), 0L));
        allocators.putId(Field.class, PhysicalId.of(ids.get(1), 0L));
        allocators.putId(TypeCategory.class, PhysicalId.of(ids.get(2), 0L));
        allocators.putId(klassNameReflectField, PhysicalId.of(ids.get(3), 0L));
        allocators.putId(TypeCategory.CLASS, PhysicalId.of(ids.get(4), 0L));

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
        allocators.allocate(2);
        allocators.putId(
                new ModelIdentity(
                        Type.class,
                        "Klass1",
                        false
                )
                , new PhysicalId(1L ,0L));
        allocators.save();
        Assert.assertEquals(10002, allocatorStore.getNextId());
    }

}