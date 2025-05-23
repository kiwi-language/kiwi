package org.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StdAllocatorTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(StdAllocatorTest.class);

    private StdAllocator allocator;

    @Override
    protected void setUp() {
        allocator = new StdAllocator(
                new MemAllocatorStore(),
                "/id/Type.properties",
                Klass.class
        );
    }

//    public void testSmoking() {
//        var ids = allocator.allocate(3);
//        allocator.putId(Klass.class.getName(), PhysicalId.of(ids.getFirst(), 0L, TestUtils.mockClassType()), null);
//        allocator.putId(Field.class.getName(), PhysicalId.of(ids.get(1), 0L, TestUtils.mockClassType()), null);
//        allocator.putId(Index.class.getName(), PhysicalId.of(ids.get(2), 0L, TestUtils.mockClassType()), null);
//        var typeId = allocator.getId(Klass.class.getName());
//        var fieldId = allocator.getId(Field.class.getName());
//        var uniqueConstraintId = allocator.getId(Index.class.getName());
//        Assert.assertEquals(PhysicalId.of(ids.getFirst(), 0L, TestUtils.mockClassType()), typeId);
//        Assert.assertEquals(PhysicalId.of(ids.get(1), 0L, TestUtils.mockClassType()), fieldId);
//        Assert.assertEquals(PhysicalId.of(ids.get(2), 0L, TestUtils.mockClassType()), uniqueConstraintId);
//    }

//    public void testIdContains() {
//        var allocatedId = allocator.allocate(1).getFirst();
//        allocator.putId(Field.class.getName(), PhysicalId.of(allocatedId, 0L, TestUtils.mockClassType()), null);
//        var fieldId = allocator.getId(Field.class.getName());
//        Assert.assertEquals(PhysicalId.of(allocatedId, 0L, TestUtils.mockClassType()), fieldId);
//        Assert.assertTrue(allocator.contains(fieldId));
//    }

    public void testTypeCode() {
        Assert.assertEquals(Klass.class, allocator.getJavaType());
    }

}