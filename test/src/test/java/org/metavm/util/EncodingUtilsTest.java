package org.metavm.util;

import junit.framework.TestCase;

import static org.junit.Assert.assertArrayEquals;

public class EncodingUtilsTest extends TestCase {

    public void testHexToBytes() {
        assertArrayEquals(new byte[]{1, 2}, EncodingUtils.hexToBytes("0102"));
    }

    public void testBytesToHex() {
        assertEquals("0102", EncodingUtils.bytesToHex(new byte[]{1, 2}));
    }

    public void testSecureHash() {
        EncodingUtils.secureHash("leen$", null);
    }


}