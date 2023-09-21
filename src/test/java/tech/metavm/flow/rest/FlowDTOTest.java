package tech.metavm.flow.rest;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.dto.RefDTO;
import tech.metavm.util.NncUtils;

public class FlowDTOTest extends TestCase {

    public void testSerialize() {
        FlowDTO flowDTO = new FlowDTO(1L,
                1L,
                "Flow1",
                "Flow1",
                false,
                false,
                false,
                RefDTO.ofId(1001L),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        String jsonStr = NncUtils.toJSONString(flowDTO);
        FlowDTO deserialized = NncUtils.readJSONString(jsonStr, FlowDTO.class);
        Assert.assertEquals(flowDTO, deserialized);
    }

}