package org.metavm.util;

import org.metavm.compiler.util.Buffer;

import java.io.IOException;
import java.io.OutputStream;

public class BufferOutput extends OutputStream  {

    private final Buffer buffer;

    public BufferOutput(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.put(b);
    }
}
