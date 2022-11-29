package tech.metavm.entity;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.meta.Type;
import tech.metavm.util.Constants;
import tech.metavm.util.TestUtils;

public class BootstrapTest extends TestCase {

    public static final String TEST_ID_DIR = "/Users/leen/workspace/object/src/test/resources";

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapTest.class);

    private final MockInstanceStore instanceStore = new MockInstanceStore();

    @Override
    protected void setUp() {
        Constants.ID_FILE_CP_ROOT = TEST_ID_DIR;
        instanceStore.clear();
        TestUtils.clearDir(TEST_ID_DIR + "/id");
    }

    public void testSmoking() {
        Bootstrap bootstrap = new Bootstrap(instanceStore, Constants.ID_FILE_CP_ROOT);
        bootstrap.boot();
        TestUtils.logJSON(
                LOGGER,
                "TypeDTO of Type.class",
                EntityTypeRegistry.getType(Type.class).toDTO()
        );
    }

}