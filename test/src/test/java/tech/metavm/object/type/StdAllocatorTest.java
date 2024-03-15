package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.PhysicalId;

public class StdAllocatorTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StdAllocatorTest.class);

    private StdAllocator allocator;

    @Override
    protected void setUp() {
        allocator = new StdAllocator(
                new MemAllocatorStore(),
                "/id/Type.properties",
                ClassType.class,
                1000L
        );
    }

    public void testSmoking() {
        var ids = allocator.allocate(3);
        allocator.putId(ClassType.class.getName(), PhysicalId.ofClass(ids.get(0), 1L));
        allocator.putId(Field.class.getName(), PhysicalId.ofClass(ids.get(1), 1L));
        allocator.putId(Index.class.getName(), PhysicalId.ofClass(ids.get(2), 1L));
        var typeId = allocator.getId(ClassType.class.getName());
        var fieldId = allocator.getId(Field.class.getName());
        var uniqueConstraintId = allocator.getId(Index.class.getName());
        Assert.assertEquals(PhysicalId.ofClass(ids.get(0), 1L), typeId);
        Assert.assertEquals(PhysicalId.ofClass(ids.get(1), 1L), fieldId);
        Assert.assertEquals(PhysicalId.ofClass(ids.get(2), 1L), uniqueConstraintId);
    }

    public void testIdContains() {
        var allocatedId = allocator.allocate(1).get(0);
        allocator.putId(Field.class.getName(), PhysicalId.ofClass(allocatedId, 1L));
        var fieldId = allocator.getId(Field.class.getName());
        Assert.assertEquals(PhysicalId.ofClass(allocatedId, 1L), fieldId);
        Assert.assertTrue(allocator.contains(fieldId));
    }

    public void testTypeCode() {
        Assert.assertEquals(ClassType.class, allocator.getJavaType());
    }

}