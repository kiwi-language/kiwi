package tech.metavm.flow.rest;

import junit.framework.TestCase;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.util.NncUtils;

import java.util.List;

public class MethodRefDTOTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(MethodRefDTOTest.class);

    public void testJsonSerialization() {
        var ref = new MethodRefDTO(
                new PhysicalId(false, 1L, 1L).toString(),
                new PhysicalId(false, 1L, 2L).toString(),
                List.of()
        );
        var deserialized = NncUtils.readJSONString(NncUtils.toJSONString(ref), FlowRefDTO.class);
        Assert.assertEquals(ref, deserialized);
    }

}