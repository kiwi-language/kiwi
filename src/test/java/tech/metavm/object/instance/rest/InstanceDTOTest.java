package tech.metavm.object.instance.rest;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.NncUtils;
import tech.metavm.util.PojoMatcher;

public class InstanceDTOTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(InstanceDTOTest.class);

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testToJSONString() {
        Instance instance = MockRegistry.getFooInstance();
        InstanceDTO instanceDTO = instance.toDTO();
        String jsonString = NncUtils.toJSONString(instanceDTO);
        InstanceDTO recoveredInstanceDTO = NncUtils.readJSONString(jsonString, InstanceDTO.class);
        MatcherAssert.assertThat(recoveredInstanceDTO, PojoMatcher.of(instanceDTO));
    }


}