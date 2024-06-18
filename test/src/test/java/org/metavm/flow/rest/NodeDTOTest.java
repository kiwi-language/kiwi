package org.metavm.flow.rest;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.metavm.flow.NodeKind;
import org.metavm.util.NncUtils;
import org.metavm.util.PojoMatcher;
import org.metavm.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NodeDTOTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(NodeDTOTest.class);

    public void testToJSONString() {
        NodeDTO nodeDTO = new NodeDTO(
                "1",
                "1",
                "Test",
                null,
                NodeKind.ADD_OBJECT.code(),
                null,
                "1",
                new AddObjectNodeParam(
                        "1",
                        true,
                        false, List.of(),
                        null,
                        null
                ),
                null,
                "1",
                null
        );
        TestUtils.logJSON(logger, nodeDTO);
        String json = NncUtils.toJSONString(nodeDTO);
        NodeDTO recoveredNodeDTO = NncUtils.readJSONString(json, NodeDTO.class);
        MatcherAssert.assertThat(recoveredNodeDTO, PojoMatcher.of(nodeDTO));
    }

}