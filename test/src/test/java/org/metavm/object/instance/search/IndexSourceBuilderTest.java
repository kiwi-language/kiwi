package org.metavm.object.instance.search;

import junit.framework.TestCase;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.MockUtils;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.metavm.util.TestConstants.APP_ID;

public class IndexSourceBuilderTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(IndexSourceBuilderTest.class);
    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var fooTypes = MockUtils.createFooTypes(true);
        var ref = new Object() {
            long nextTreeId = 1000000;
        };
        var instance = MockUtils.createFoo(fooTypes, () -> PhysicalId.of(ref.nextTreeId++, 0));
        Map<String, Object> source = IndexSourceBuilder.buildSource(APP_ID, instance);
        TestUtils.logJSON(logger, source);
    }

}