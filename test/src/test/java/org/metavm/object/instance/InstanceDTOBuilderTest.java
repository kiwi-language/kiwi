package org.metavm.object.instance;

import junit.framework.TestCase;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.util.MockUtils;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceDTOBuilderTest extends TestCase {

    private static final Logger logger = LoggerFactory.getLogger(InstanceDTOBuilderTest.class);

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var foo = MockUtils.createFoo(MockUtils.createFooTypes(true), true);
        InstanceDTO instanceDTO = InstanceDTOBuilder.buildDTO(foo.getReference(), 2);
        TestUtils.logJSON(logger, instanceDTO);
    }
}