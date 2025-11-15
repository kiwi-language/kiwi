package org.metavm.compiler.util;

import org.metavm.compiler.generate.KlassOutput;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Consumer;

public class Buffer {

    private byte[] bytes = new byte[16];
    private int length;

    public void put(int b) {
        if (length >= bytes.length)
            bytes = Arrays.copyOf(bytes, bytes.length * 2);
        bytes[length++] = (byte) b;
    }

    public void put(int i, int b) {
        assert i < length;
        bytes[i] = (byte) b;
    }

    public void putShort(int s) {
        put(s >> 8 & 0xff);
        put(s & 0xff);
    }

    public void putVarInt(int i) {
        putVarLong(i);
    }

    public void putVarLong(long l) {
        //noinspection DuplicatedCode
        long sign;
        if (l < 0) {
            sign = 1;
            l = -l;
        } else
            sign = 0;
        int b = (int) ((l & 0x3f) << 1 | sign);
        l >>>= 6;
        for (; l != 0; l >>>= 7) {
            b |= 0x80;
            put(b);
            b = (int) (l & 0x7f);
        }
        put(b);
    }

    public void putUTF(String s) {
        var bs = s.getBytes(StandardCharsets.UTF_8);
        putBytes(bs, 0, bs.length);
    }

    public void putBytes(byte[] b, int offset, int len) {
        var cap = bytes.length;
        var required = length + len;
        if (cap < required) {
            do {
                cap <<= 1;
            } while (cap < required);
            bytes = Arrays.copyOf(bytes, cap);
        }
        System.arraycopy(b, offset, bytes, length, len);
        length += len;
    }

    public void putBuffer(Buffer buffer) {
        putBytes(buffer.bytes, 0, bytes.length);
    }

    public <E> void putList(List<E> list, Consumer<? super E> action) {
        putVarInt(list.size());
        for (E e : list) {
            action.accept(e);
        }
    }

    public void putBoolean(boolean b) {
        put(b ? 1 : 0);
    }

    public void putDouble(Double d) {
        long l = Double.doubleToRawLongBits(d);
        for (int s = 0; s < 64; s += 8)
            put((int) (l >> s & 0xff));
    }

    public void putFloat(Float f) {
        putVarInt(Float.floatToRawIntBits(f));
    }

    public <E> void putNullable(@Nullable E value, Consumer<E> action) {
        if (value != null) {
            putBoolean(true);
            action.accept(value);
        } else
            putBoolean(false);
    }

    public void clear() {
        length = 0;
    }

    public void write(KlassOutput output) {
        output.writeInt(length);
        output.write(bytes, 0, length);
    }

    public int length() {
        return length;
    }
}
