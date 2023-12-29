package tech.metavm.object.type;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.persistence.PersistenceUtils;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.PojoMatcher;
import tech.metavm.util.TestUtils;

import static tech.metavm.util.TestConstants.APP_ID;

public class ValueFormatterTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ValueFormatterTest.class);

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testFormat() {
        try (var context = MockRegistry.newEntityContext(APP_ID)) {
            ClassInstance instance = MockRegistry.getFooInstance();
            for (Field field : instance.getType().getAllFields()) {
                Object fieldValue = ValueFormatter.format(instance.getField(field), context.getInstanceContext());
                TestUtils.logJSON(LOGGER, field.getName(), fieldValue);
            }
        }
    }

}