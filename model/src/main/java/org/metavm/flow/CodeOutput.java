package org.metavm.flow;

import org.metavm.object.type.ConstantPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CodeOutput {

    private final ConstantPool constantPool;
    private final ByteArrayOutputStream bout = new ByteArrayOutputStream();

    public CodeOutput(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }

    public void write(int b) {
        bout.write(b);
    }

    public void write(byte[] bytes) {
        try {
            bout.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeShort(int s) {
        write(s >> 8 & 0xff);
        write(s & 0xff);
    }

    public void writeInt(int i) {
        write(i >> 24 & 0xff);
        write(i >> 16 & 0xff);
        write(i >> 8 & 0xff);
        write(i & 0xff);
    }

    public void writeLong(long l) {
        write((int) (l >> 56 & 0xff));
        write((int) (l >> 48 & 0xff));
        write((int) (l >> 40 & 0xff));
        write((int) (l >> 32 & 0xff));
        write((int) (l >> 24 & 0xff));
        write((int) (l >> 16 & 0xff));
        write((int) (l >> 8 & 0xff));
        write((int) (l & 0xff));
    }

    public void writeBoolean(boolean b) {
        write(b ? 1 : 0);
    }

    public void writeFloat(float f) {
        writeInt(Float.floatToRawIntBits(f));
    }

    public void writeDouble(double d) {
        writeLong(Double.doubleToRawLongBits(d));
    }

    public void writeString(String s) {
        var bs = s.getBytes(StandardCharsets.UTF_8);
        writeInt(bs.length);
        write(bs);
    }

    public void writeConstant(Object value) {
        writeShort(constantPool.addValue(value));
    }

    public byte[] toByteArray() {
        return bout.toByteArray();
    }

}
