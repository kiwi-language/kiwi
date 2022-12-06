package tech.metavm.object.meta;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.MemInstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.MockRegistry;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.PojoMatcher;
import tech.metavm.util.TestUtils;

import static tech.metavm.util.TestConstants.TENANT_ID;

public class ValueFormatterTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ValueFormatterTest.class);

    @Override
    protected void setUp() throws Exception {
        MockRegistry.setUp(new MockIdProvider());
    }

    public void testParse() {
        Instance instance = MockRegistry.getFooInstance();
        InstanceDTO instanceDTO = (InstanceDTO) ValueFormatter.format(instance.toDTO(), instance.getType());

        MemInstanceContext context = new MemInstanceContext();
        context.setTypeProvider(MockRegistry::getType);
        context.replace(instance);

        Instance recoveredInst = ValueFormatter.parseInstance(instanceDTO, context);

        Assert.assertNotNull(recoveredInst);

        MatcherAssert.assertThat(recoveredInst.toPO(TENANT_ID), PojoMatcher.of(instance.toPO(TENANT_ID)));
    }

    public void testFormat() {
        Instance instance = MockRegistry.getFooInstance();
        for (Field field : instance.getType().getFields()) {
            Object fieldValue = ValueFormatter.format(instance.get(field), field.getType());
            TestUtils.logJSON(LOGGER, field.getName(), fieldValue);
        }

    }

}