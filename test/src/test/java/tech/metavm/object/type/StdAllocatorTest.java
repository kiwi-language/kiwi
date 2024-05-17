package tech.metavm.object.type;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.DefaultPhysicalId;
import tech.metavm.util.TestUtils;

public class StdAllocatorTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StdAllocatorTest.class);

    private StdAllocator allocator;

    @Override
    protected void setUp() {
        allocator = new StdAllocator(
                new MemAllocatorStore(),
                "/id/Type.properties",
                Klass.class,
                1000L
        );
    }

    public void testSmoking() {
        var ids = allocator.allocate(3);
        allocator.putId(Klass.class.getName(), DefaultPhysicalId.ofObject(ids.get(0), 0L, TestUtils.mockClassType()));
        allocator.putId(Field.class.getName(), DefaultPhysicalId.ofObject(ids.get(1), 0L, TestUtils.mockClassType()));
        allocator.putId(Index.class.getName(), DefaultPhysicalId.ofObject(ids.get(2), 0L, TestUtils.mockClassType()));
        var typeId = allocator.getId(Klass.class.getName());
        var fieldId = allocator.getId(Field.class.getName());
        var uniqueConstraintId = allocator.getId(Index.class.getName());
        Assert.assertEquals(DefaultPhysicalId.ofObject(ids.get(0), 0L, TestUtils.mockClassType()), typeId);
        Assert.assertEquals(DefaultPhysicalId.ofObject(ids.get(1), 0L, TestUtils.mockClassType()), fieldId);
        Assert.assertEquals(DefaultPhysicalId.ofObject(ids.get(2), 0L, TestUtils.mockClassType()), uniqueConstraintId);
    }

    public void testIdContains() {
        var allocatedId = allocator.allocate(1).get(0);
        allocator.putId(Field.class.getName(), DefaultPhysicalId.ofObject(allocatedId, 0L, TestUtils.mockClassType()));
        var fieldId = allocator.getId(Field.class.getName());
        Assert.assertEquals(DefaultPhysicalId.ofObject(allocatedId, 0L, TestUtils.mockClassType()), fieldId);
        Assert.assertTrue(allocator.contains(fieldId));
    }

    public void testTypeCode() {
        Assert.assertEquals(Klass.class, allocator.getJavaType());
    }

}