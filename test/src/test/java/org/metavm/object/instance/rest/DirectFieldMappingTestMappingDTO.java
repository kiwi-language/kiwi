package org.metavm.object.instance.rest;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.metavm.flow.rest.ValueDTO;
import org.metavm.object.type.PrimitiveKind;
import org.metavm.util.PojoMatcher;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectFieldMappingTestMappingDTO extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(DirectFieldMappingTestMappingDTO.class);

    public void testToJSON() {
        ValueDTO value = ValueDTO.constValue(new PrimitiveFieldValue("", PrimitiveKind.LONG.code(), 1));
        ValueDTO recoveredValue = TestUtils.readJSON(ValueDTO.class, TestUtils.toJSONString(value));
        MatcherAssert.assertThat(recoveredValue, PojoMatcher.of(value));
//        TestUtils.logJSON(logger, value);
    }

}