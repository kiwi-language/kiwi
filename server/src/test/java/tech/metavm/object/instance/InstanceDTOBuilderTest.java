package tech.metavm.object.instance;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.MockUtils;
import tech.metavm.util.TestUtils;

public class InstanceDTOBuilderTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceDTOBuilderTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();;
    }

    public void test() {
        var foo = MockUtils.createFoo(MockUtils.createFooTypes(true), true);
        InstanceDTO instanceDTO = InstanceDTOBuilder.buildDTO(foo, 2);
        TestUtils.logJSON(LOGGER, instanceDTO);
    }
}