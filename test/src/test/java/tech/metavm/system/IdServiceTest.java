package tech.metavm.system;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.IdConstants;
import tech.metavm.util.BootstrapUtils;
import tech.metavm.util.TestConstants;

import java.util.Map;

public class IdServiceTest extends TestCase {

    private IdService idService;

    @Override
    protected void setUp() throws Exception {
        var bootResult = BootstrapUtils.bootstrap();
        var regionManager = new RegionManager(bootResult.regionMapper());
        idService = new IdService(bootResult.blockMapper(), regionManager);
    }

    @Override
    protected void tearDown() throws Exception {
        idService = null;
    }

    public void test() {
        var type = ModelDefRegistry.getType(Klass.class);
        int count = (int) IdConstants.DEFAULT_BLOCK_SIZE / 2 + 1;
        var ids = idService.allocate(TestConstants.APP_ID, Map.of(type, count)).get(type);
        Assert.assertEquals(count, ids.size());
        ids = idService.allocate(TestConstants.APP_ID, Map.of(type, count)).get(type);
        Assert.assertEquals(count, ids.size());
    }

}