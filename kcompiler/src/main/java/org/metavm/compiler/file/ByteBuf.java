package org.metavm.compiler.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ByteBuf {
    private byte[] bytes = new byte[64];
    private int len;

    public void read(InputStream in) throws IOException {
        for (;;) {
            var toRead = Math.max(in.available(), 64);
            ensureCap(len + toRead);
            var n = in.read(bytes, len, toRead);
            if (n == -1)
                break;
            len += n;
        }
    }

    public CharBuffer decode(Charset charset) {
        var bb  = ByteBuffer.wrap(bytes, 0, len);
        return charset.decode(bb);
    }

    public int length() {
        return len;
    }

    public void clear() {
        len = 0;
    }

    private void ensureCap(int cap) {
        if (bytes.length < cap) {
            var newCap = bytes.length * 2;
            while (newCap < cap)
                newCap *= 2;
            bytes = Arrays.copyOf(bytes, newCap);
        }
    }

}
