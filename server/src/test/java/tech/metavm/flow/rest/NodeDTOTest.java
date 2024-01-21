package tech.metavm.flow.rest;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.NodeKind;
import tech.metavm.util.NncUtils;
import tech.metavm.util.PojoMatcher;
import tech.metavm.common.RefDTO;
import tech.metavm.util.TestUtils;

import java.util.List;

public class NodeDTOTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(NodeDTOTest.class);

    public void testToJSONString() {
        NodeDTO nodeDTO = new NodeDTO(
                1L,
                NncUtils.randomNonNegative(),
                1L,
                "Test",
                null,
                NodeKind.ADD_OBJECT.code(),
                null,
                RefDTO.fromId(1L),
                new AddObjectNodeParam(
                        RefDTO.fromId(1L),
                        true,
                        false, List.of(),
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