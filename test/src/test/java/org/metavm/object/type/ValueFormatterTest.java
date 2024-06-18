package org.metavm.object.type;

import junit.framework.TestCase;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.util.MockUtils;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueFormatterTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(ValueFormatterTest.class);

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void testFormat() {
        var fooTypes = MockUtils.createFooTypes(true);
        var instance = MockUtils.createFoo(fooTypes);
        for (Field field : instance.getKlass().getAllFields()) {
            Object fieldValue = ValueFormatter.format(instance.getField(field));
            TestUtils.logJSON(logger, field.getName(), fieldValue);
        }
    }

}