package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.util.Constants;

public class StdAllocatorTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(StdAllocatorTest.class);

    private StdAllocator allocator;

    @Override
    protected void setUp() {
        allocator = new StdAllocator(
                Constants.ID_FILE_CP_ROOT,
                "/id/Type.properties"
        );
    }

    public void testSmoking() {
        long typeId = allocator.getId(Type.class.getName());
        LOGGER.info("typeId: " + typeId);
        long fieldId = allocator.getId(Field.class.getName());
        LOGGER.info("fieldId: " + fieldId);
        long uniqueConstraintId = allocator.getId(UniqueConstraintRT.class.getName());
        LOGGER.info("uniqueConstraintId: " + uniqueConstraintId);
        allocator.save();
    }

    public void testIdContains() {
        long fieldId = allocator.getId(Field.class.getName());
        Assert.assertTrue(allocator.contains(fieldId));
    }

    public void testTypeCode() {
        Assert.assertEquals(Type.class, allocator.getJavaType());
    }

}