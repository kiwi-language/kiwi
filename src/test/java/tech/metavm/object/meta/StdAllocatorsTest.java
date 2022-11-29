package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.util.Constants;
import tech.metavm.util.ReflectUtils;

public class StdAllocatorsTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(StdAllocatorTest.class);

    private StdAllocators allocators;

    @Override
    protected void setUp() {
        allocators = new StdAllocators(Constants.ID_FILE_CP_ROOT);
    }

    public void testSmoking() {
        long fieldClassId = allocators.getId(Field.class);
        LOGGER.info("Field id: " + fieldClassId);

        long typeNameFieldId = allocators.getId(ReflectUtils.getField(Type.class, "name"));
        LOGGER.info("Type.name id: " + typeNameFieldId);
    }

    public void testGetTypeId() {
        long typeClassId = allocators.getId(Type.class);
        long fieldClassId = allocators.getId(Field.class);
        Assert.assertEquals(typeClassId, allocators.getTypeId(fieldClassId));

        long typeNameFieldId = allocators.getId(ReflectUtils.getField(Type.class, "name"));
        Assert.assertEquals(fieldClassId, allocators.getTypeId(typeNameFieldId));

        long typeCategoryClassId = allocators.getId(TypeCategory.class);
        Assert.assertEquals(typeClassId, allocators.getTypeId(typeCategoryClassId));

        long enumConstantId = allocators.getId(TypeCategory.CLASS);
        Assert.assertEquals(typeCategoryClassId, allocators.getTypeId(enumConstantId));

        allocators.save();
    }

}