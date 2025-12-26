package org.metavm.server;

import junit.framework.TestCase;
import org.metavm.compiler.util.List;

public class QueriesTest extends TestCase {

    public void testParseQuery() {
        var params = Queries.parseQuery("name=leen&gender=male");
        assertEquals(List.of("leen"), params.get("name"));
        assertEquals(List.of("male"), params.get("gender"));
    }

}
