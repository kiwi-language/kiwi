package org.metavm.system;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.IdConstants;
import org.metavm.object.type.Klass;
import org.metavm.util.TestConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class IdServiceTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(IdServiceTest.class);

    private IdService idService;

    @Override
    protected void setUp() throws Exception {
        idService = new IdService(new IdGenerator(new MemoryBlockRepository()));
    }

    @Override
    protected void tearDown() throws Exception {
        idService = null;
    }

    public void test() {
        int count = (int) IdConstants.DEFAULT_BLOCK_SIZE / 2 + 1;
        var ids = idService.allocate(TestConstants.APP_ID, count);
        Assert.assertEquals(count, ids.size());
        ids = idService.allocate(TestConstants.APP_ID, count);
        Assert.assertEquals(count, ids.size());
    }

    public void testAllocate() {
        var ids = idService.allocate(TestConstants.APP_ID, 20);
        logger.info("{}", ids);
    }

}