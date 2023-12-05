package tech.metavm.object.instance.rest;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.util.PojoMatcher;
import tech.metavm.util.TestUtils;

public class FieldValueDTOTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(FieldValueDTOTest.class);

    public void testToJSON() {
        ValueDTO value = ValueDTO.constValue(new PrimitiveFieldValue("", PrimitiveKind.LONG.code(), 1));
        ValueDTO recoveredValue = TestUtils.readJSON(ValueDTO.class, TestUtils.toJSONString(value));
        MatcherAssert.assertThat(recoveredValue, PojoMatcher.of(value));
//        TestUtils.logJSON(LOGGER, value);
    }

}