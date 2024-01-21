package tech.metavm.object.instance.rest;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.*;

public class InstanceDTOTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceDTOTest.class);

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