package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.Entity;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.IdTag;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.function.Consumer;

@Slf4j
public abstract class MvOutput extends OutputStream {

    private final OutputStream out;

    protected MvOutput(OutputStream out) {
        this.out = out;
    }

    public void write(int b) {
        try {
            out.write(b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(byte @NotNull [] bytes) {
        try {
            out.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeShort(int s) {
        write(s >> 8 & 0xff);
        write(s & 0xff);
    }

    public void writeInt(int i) {
        writeLong(i);
    }

    public void writeLong(long l) {
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
            write(b);
            b = (int) (l & 0x7f);
        }
        write(b);
    }

    public void writeDouble(double d) {
        long l = Double.doubleToRawLongBits(d);
        for (int s = 0; s < 64; s += 8)
            write((int) (l >> s & 0xff));
    }

    public void writeFloat(float f) {
        writeInt(Float.floatToRawIntBits(f));
    }

    public void writeUTF(String s) {
        var bytes = s.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length);
        write(bytes);
    }

    public void writeId(Id id) {
        id.write(this);
    }

    public void writeEntityId(Entity entity) {
        writeId(entity.getId());
    }

    protected OutputStream getOut() {
        return out;
    }

    public void writeIdTag(IdTag idTag, boolean isArray) {
        write(idTag.maskedCode(isArray));
    }

    public void writeBoolean(boolean b) {
        write(b ? 1 : 0);
    }

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

    public <T> void writeList(Collection<? extends T> list, Consumer<T> write) {
        writeInt(list.size());
        list.forEach(write);
    }
}
