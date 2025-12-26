package org.metavm.wire;

import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class DefaultWireInput implements WireInput, Closeable {

    private final InputStream input;

    public DefaultWireInput(InputStream input) {
        this.input = input;
    }

    @Override
    public byte readByte() {
        return (byte) read();
    }

    @Override
    public short readShort() {
        return (short) (read() << 8 | read());

    }

    @Override
    public int readInt() {
        return (int) readLong();
    }

    @Override
    public int readFixedInt() {
        return (read() & 0xff) << 24 | (read() & 0xff) << 16 | (read() & 0xff) << 8 | read() & 0xff;
    }

    @Override
    public long readLong() {
        int b = read();
        boolean negative = (b & 1) == 1;
        long v = b >> 1 & 0x3f;
        int shifts = 6;
        for (int i = 0; (b & 0x80) != 0 && i < 10; i++, shifts += 7) {
            b = read();
            v |= (long) (b & 0x7f) << shifts;
        }
        return negative ? -v : v;
    }

    @Override
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() {
        long l = 0;
        for (int shifts = 0; shifts < 64; shifts += 8)
            l |= (long) read() << shifts;
        return Double.longBitsToDouble(l);
    }

    @Override
    public char readChar() {
        int firstByte = read();
        if (firstByte == -1)
            throw new WireIOException("End of stream reached");
        if ((firstByte & 0x80) == 0) {
            return (char) firstByte;
        } else if ((firstByte & 0xE0) == 0xC0) {
            int secondByte = read();
            return (char) (((firstByte & 0x1F) << 6) | (secondByte & 0x3F));
        } else if ((firstByte & 0xF0) == 0xE0) {
            int secondByte = read();
            int thirdByte = read();
            return (char) (((firstByte & 0x0F) << 12) | ((secondByte & 0x3F) << 6) | (thirdByte & 0x3F));
        } else
            throw new WireIOException("Invalid UTF-8 byte sequence");
    }

    @Override
    public boolean readBoolean() {
        return read() != 0;
    }

    @Override
    public String readString() {
        int len = readInt();
        byte[] bytes = new byte[len];
        read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public Date readDate() {
        return new Date(readLong());
    }

    @Override
    public byte[] readBytes() {
        var buf = new byte[readInt()];
        read(buf);
        return buf;
    }

    @Override
    public <T> T readEntity(WireAdapter<T> adapter, @Nullable Object parent) {
        return adapter.read(this, parent);
    }

    @Override
    public Object readEntity() {
        return readEntity(null);
    }

    @Override
    public Object readEntity(Object parent) {
        return readEntity(AdapterRegistry.instance.getObjectAdapter(), parent);
    }


    @Override
    public <T> T readNullable(Supplier<T> read) {
        return readBoolean() ? read.get() : null;
    }

    @Override
    public <T> List<T> readList(Supplier<T> readElement) {
        var size = readInt();
        var list = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            list.add(readElement.get());
        }
        return list;
    }

    @Override
    public <T> T[] readArray(Supplier<T> readElement, IntFunction<T[]> generator) {
        var size = readInt();
        var array = generator.apply(size);
        for (int i = 0; i < size; i++) {
            array[i] = readElement.get();
        }
        return array;
    }

    @SneakyThrows
    @Override
    public int read() {
        return input.read();
    }

    @SneakyThrows
    public int read(byte[] buf) {
        int offset = 0;
        int len = buf.length;
        while (offset < len) {
            offset += input.read(buf, offset, len - offset);
        }
        return len;
    }


    @SneakyThrows
    public void skip(int len) {
        input.skip(len);
    }


    @Override
    public void close() throws IOException {
        input.close();
    }

    public InputStream getIn() {
        return input;
    }
}
