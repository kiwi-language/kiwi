package tech.metavm.object.type;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.util.MockUtils;
import tech.metavm.util.TestUtils;

public class ValueFormatterTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(ValueFormatterTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testFormat() {
        var fooTypes = MockUtils.createFooTypes(true);
        var instance = MockUtils.createFoo(fooTypes);
        for (Field field : instance.getType().getAllFields()) {
            Object fieldValue = ValueFormatter.format(instance.getField(field));
            TestUtils.logJSON(LOGGER, field.getName(), fieldValue);
        }
    }

}