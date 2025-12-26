package org.metavm.server;

import junit.framework.TestCase;

public class PathPatternTest extends TestCase {

    public void testPathVariable() {
        var ptn = new PathPattern("/{appId}");
        var pathVars = ptn.match("/123");
        assertNotNull(pathVars);
        assertEquals("123", pathVars.get("appId"));
        assertNull(ptn.match("/app/123"));
    }

    public void testWildcard() {
        var ptn = new PathPattern("/*");
        assertNotNull(ptn.match("/app"));
        assertNull(ptn.match("/app/123"));
    }

    public void testDoubleWildcards() {
        var ptn = new PathPattern("/**");
        assertNotNull(ptn.match("/app"));
        assertNotNull(ptn.match("/app/123"));
    }

    public void testSlash() {
        var ptn = new PathPattern("/");
        assertNotNull(ptn.match("/"));
        assertNotNull(ptn.match(""));
    }

}