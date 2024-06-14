package org.metavm.object.instance;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.util.MockUtils;
import org.metavm.util.TestUtils;

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