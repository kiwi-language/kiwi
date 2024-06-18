package org.metavm.object.instance.rest;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.util.MockUtils;
import org.metavm.util.NncUtils;
import org.metavm.util.PojoMatcher;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceDTOTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(InstanceDTOTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testToJSONString() {
        var fooTypes = MockUtils.createFooTypes(true);
        var instance = MockUtils.createFoo(fooTypes);
        TestUtils.initInstanceIds(instance);
        InstanceDTO instanceDTO = instance.toDTO();
        String jsonString = NncUtils.toJSONString(instanceDTO);
        InstanceDTO recoveredInstanceDTO = NncUtils.readJSONString(jsonString, InstanceDTO.class);
        MatcherAssert.assertThat(recoveredInstanceDTO, PojoMatcher.of(instanceDTO));
    }


}