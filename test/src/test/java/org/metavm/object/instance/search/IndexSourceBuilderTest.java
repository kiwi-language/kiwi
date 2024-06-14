package org.metavm.object.instance.search;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.util.MockUtils;
import org.metavm.util.TestUtils;

import java.util.Map;

import static org.metavm.util.TestConstants.APP_ID;

public class IndexSourceBuilderTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(IndexSourceBuilderTest.class);
    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var fooTypes = MockUtils.createFooTypes(true);
        var instance = MockUtils.createFoo(fooTypes);
        TestUtils.initInstanceIds(instance);
        Map<String, Object> source = IndexSourceBuilder.buildSource(APP_ID, instance);
        TestUtils.logJSON(LOGGER, source);
    }

}