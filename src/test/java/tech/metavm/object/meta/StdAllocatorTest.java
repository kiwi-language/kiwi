package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StdAllocatorTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StdAllocatorTest.class);

    private StdAllocator allocator;

    @Override
    protected void setUp() {
        allocator = new StdAllocator(
                new MemAllocatorStore(),
                "/id/Type.properties",
                Type.class,
                1000L
        );
    }

    public void testSmoking() {
        List<Long> ids = allocator.allocate(3);
        allocator.putId(Type.class.getName(), ids.get(0));
        allocator.putId(Field.class.getName(), ids.get(1));
        allocator.putId(UniqueConstraintRT.class.getName(), ids.get(2));
        long typeId = allocator.getId(Type.class.getName());
        long fieldId = allocator.getId(Field.class.getName());
        long uniqueConstraintId = allocator.getId(UniqueConstraintRT.class.getName());
        Assert.assertEquals((long) ids.get(0), typeId);
        Assert.assertEquals((long) ids.get(1), fieldId);
        Assert.assertEquals((long) ids.get(2), uniqueConstraintId);
    }

    public void testIdContains() {
        long allocatedId = allocator.allocate(1).get(0);
        allocator.putId(Field.class.getName(), allocatedId);
        long fieldId = allocator.getId(Field.class.getName());
        Assert.assertEquals(allocatedId, fieldId);
        Assert.assertTrue(allocator.contains(fieldId));
    }

    public void testTypeCode() {
        Assert.assertEquals(Type.class, allocator.getJavaType());
    }

}