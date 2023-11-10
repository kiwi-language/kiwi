package tech.metavm.flow.rest;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.NodeKind;
import tech.metavm.util.NncUtils;
import tech.metavm.util.PojoMatcher;
import tech.metavm.util.TestUtils;
import tech.metavm.dto.RefDTO;

import java.util.List;

public class NodeDTOTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(NodeDTOTest.class);

    public void testToJSONString() {
        NodeDTO nodeDTO = new NodeDTO(
                null,
                1L,
                1L,
                "Test",
                NodeKind.ADD_OBJECT.code(),
                null,
                RefDTO.ofId(1L),
                new AddObjectParam(
                        RefDTO.ofId(1L),
                        true,
                        List.of(),
                        null,
                        null
                ),
                null,
                1L,
                null
        );
        TestUtils.logJSON(LOGGER, nodeDTO);
        String json = NncUtils.toJSONString(nodeDTO);
        NodeDTO recoveredNodeDTO = NncUtils.readJSONString(json, NodeDTO.class);
        MatcherAssert.assertThat(recoveredNodeDTO, PojoMatcher.of(nodeDTO));
    }

}