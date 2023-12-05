package tech.metavm.object.instance.search;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.TestUtils;

import java.util.Map;

import static tech.metavm.util.TestConstants.APP_ID;

public class IndexSourceBuilderTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(IndexSourceBuilderTest.class);
    private MockIdProvider idProvider;
    @Override
    protected void setUp() throws Exception {
        idProvider = new MockIdProvider();
        MockRegistry.setUp(idProvider);
    }

    public void test() {
        ClassInstance instance = MockRegistry.getFooInstance();
        Map<String, Object> source = IndexSourceBuilder.buildSource(APP_ID, instance);
        TestUtils.logJSON(LOGGER, source);
    }

}