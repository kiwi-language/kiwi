package tech.metavm.flow.rest;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.common.RefDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

public class FlowDTOTest extends TestCase {

    public void testSerialize() {
        FlowDTO flowDTO = new FlowDTO(1L,
                1L,
                "Flow1",
                "Flow1",
                false,
                false,
                false,
                RefDTO.fromId(1001L),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                List.of(),
                false
        );
        String jsonStr = NncUtils.toJSONString(flowDTO);
        FlowDTO deserialized = NncUtils.readJSONString(jsonStr, FlowDTO.class);
        Assert.assertEquals(flowDTO, deserialized);
    }

}