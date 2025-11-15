package org.metavm.wire;

import lombok.SneakyThrows;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class DefaultWireOutput extends OutputStream implements WireOutput {
    private final OutputStream out;

    public DefaultWireOutput(OutputStream out) {
        this.out = out;
    }

    @SneakyThrows
    @Override
    public void write(int b) {
        out.write(b);
    }

    @SneakyThrows
    @Override
    public void write(byte[] b) {
        out.write(b);
    }

    @SneakyThrows
    @Override
    public void write(byte[] bytes, int offset, int length) {
        out.write(bytes, offset, length);
    }


    @Override
    public void writeBoolean(boolean b) {
        write(b ? 1 : 0);
    }

    @Override
    public void writeChar(char c) {
        if (c <= 0x7F) {
            write(c);
        } else if (c <= 0x7FF) {
            write(0xC0 | (c >> 6));
            write(0x80 | (c & 0x3F));
        } else {
            write(0xE0 | (c >> 12));
            write(0x80 | ((c >> 6) & 0x3F));
            write(0x80 | (c & 0x3F));
        }

    }

    @Override
    public void writeByte(byte v) {
        write(v);
    }

    @Override
    public void writeShort(int v) {
        write(v >> 8 & 0xff);
        write(v & 0xff);
    }

    @Override
    public void writeInt(int v) {
        writeLong(v);
    }

    @Override
    public void writeFixedInt(int v) {
        write(v >> 24 & 0xff);
        write(v >> 16 & 0xff);
        write(v >> 8 & 0xff);
        write(v & 0xff);
    }

    @Override
    public void writeLong(long v) {
        long sign;
        if (v < 0) {
            sign = 1;
            v = -v;
        } else
            sign = 0;
        int b = (int) ((v & 0x3f) << 1 | sign);
        v >>>= 6;
        for (; v != 0; v >>>= 7) {
            b |= 0x80;
            write(b);
            b = (int) (v & 0x7f);
        }
        write(b);
    }

    @Override
    public void writeFloat(float v) {
        writeInt(Float.floatToRawIntBits(v));
    }

    @Override
    public void writeDouble(double v) {
        long l = Double.doubleToRawLongBits(v);
        for (int s = 0; s < 64; s += 8)
            write((int) (l >> s & 0xff));
    }

    @Override
    public void writeString(String s) {
        var bytes = s.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length);
        write(bytes);
    }

    @Override
    public void writeDate(Date date) {
        writeLong(date.getTime());
    }

    @Override
    public void writeBytes(byte[] b) {
        writeInt(b.length);
        write(b);
    }

    @Override
    public <T> void writeEntity(T o, WireAdapter<T> adapter) {
        adapter.write(o, this);
    }

    public void writeEntity(Object o) {
        writeEntity(o, AdapterRegistry.instance.getObjectAdapter());
    }

    @Override
    public <T> void writeNullable(T o, Consumer<? super T> write) {
        if (o != null) {
            writeBoolean(true);
            write.accept(o);
        }
        else
            writeBoolean(false);

    }

    @Override
    public <E> void writeList(List<E> list, Consumer<? super E> write) {
        writeInt(list.size());
        list.forEach(write);
    }

    @Override
    public <E> void writeArray(E[] array, Consumer<? super E> write) {
        writeInt(array.length);
        for (E e : array) {
            write.accept(e);
        }
    }

    public OutputStream getOut() {
        return out;
    }
}
