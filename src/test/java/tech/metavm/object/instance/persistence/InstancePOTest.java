package tech.metavm.object.instance.persistence;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.util.NncUtils;
import tech.metavm.util.PojoMatcher;
import tech.metavm.util.TestUtils;

import java.util.List;

public class InstancePOTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstancePOTest.class);

    public void testToJSON() {
        InstancePO instancePO = new InstanceArrayPO(1L, 1L, -1L, 0, List.of(), null, null, 0L, 0L);
        String jsonString = NncUtils.toJSONString(instancePO);
        InstancePO recovered = NncUtils.readJSONString(jsonString, InstancePO.class);
        MatcherAssert.assertThat(instancePO, PojoMatcher.of(recovered));
    }

}