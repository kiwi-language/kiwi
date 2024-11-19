package org.metavm.util;

import org.metavm.flow.Function;
import org.metavm.flow.Lambda;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TypeTag;
import org.metavm.object.type.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class MvInput implements Closeable {

    private final InputStream in;

    public MvInput(InputStream in) {
        this.in = in;
    }

    public int read(byte[] buf) {
        try {
            int offset = 0;
            int len = buf.length;
            while (offset < len) {
                offset += in.read(buf, offset, len - offset);
            }
            return len;
        } catch (IOException e) {
            throw new InternalException("Failed to read from the underlying input steam", e);
        }
    }

    public int read() {
        try {
            return in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void skip(int len) {
        try {
            //noinspection ResultOfMethodCallIgnored
            in.skip(len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    public InputStream getIn() {
        return in;
    }

    public String readUTF() {
        int len = readInt();
        byte[] bytes = new byte[len];
        read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public int readShort() {
        return read() << 8 | read();
    }

    public int readInt() {
        return (int) readLong();
    }

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

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public TypeTag readTypeTag() {
        return TypeTag.fromCode(read());
    }

    public double readDouble() {
        long l = 0;
        for (int shifts = 0; shifts < 64; shifts += 8)
            l |= (long) read() << shifts;
        return Double.longBitsToDouble(l);
    }

    public boolean readBoolean() {
        return read() != 0;
    }

    public char readChar() {
        int firstByte = read();
        if (firstByte == -1)
            throw new InternalException("End of stream reached");
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
            throw new InternalException("Invalid UTF-8 byte sequence");
    }

    public Id readId() {
        return Id.readId(this);
    }

    public <T> List<T> readList(Supplier<T> read) {
        var size = readInt();
        var list = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            list.add(read.get());
        }
        return list;
    }

    public abstract Klass getKlass(Id id);

    public abstract Method getMethod(Id id);

    public abstract Field getField(Id id);

    public abstract TypeVariable getTypeVariable(Id id);

    public abstract Function getFunction(Id id);

    public abstract CapturedTypeVariable getCapturedTypeVariable(Id id);

    public abstract Lambda getLambda(Id id);

    public abstract Index getIndex(Id id);

    public abstract IndexField getIndexField(Id id);

    public abstract EnumConstantDef getEnumConstantDef(Id id);
}
