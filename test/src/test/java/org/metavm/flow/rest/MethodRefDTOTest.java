package org.metavm.flow.rest;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.NncUtils;

import java.util.List;

public class MethodRefDTOTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(MethodRefDTOTest.class);

    public void testJsonSerialization() {
        var ref = new MethodRefDTO(
                Constants.addIdPrefix(new PhysicalId(false, 1L, 1L).toString()),
                Constants.addIdPrefix(new PhysicalId(false, 1L, 2L).toString()),
                List.of()
        );
        var deserialized = NncUtils.readJSONString(NncUtils.toJSONString(ref), FlowRefDTO.class);
        Assert.assertEquals(ref, deserialized);
    }

}