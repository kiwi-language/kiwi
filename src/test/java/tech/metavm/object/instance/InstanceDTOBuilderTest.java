package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.TestUtils;

public class InstanceDTOBuilderTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceDTOBuilderTest.class);

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void test() {
        ClassInstance foo = MockRegistry.getFooInstance();
        InstanceDTO instanceDTO = InstanceDTOBuilder.buildDTO(foo, 2);
        TestUtils.logJSON(LOGGER, instanceDTO);
    }
}