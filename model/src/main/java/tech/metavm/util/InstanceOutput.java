package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.IdTag;
import tech.metavm.object.instance.core.Instance;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class InstanceOutput extends OutputStream {

    public static byte[] toBytes(DurableInstance instance) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeMessage(instance);
        return bout.toByteArray();
    }

    private final OutputStream outputStream;

    public InstanceOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeMessage(DurableInstance instance) {
        writeLong(instance.getVersion());
        writeLong(instance.getTreeId());
        writeLong(instance.getNextNodeId());
        writeRecord(instance);
    }

    public void writeRecord(Instance instance) {
        instance.writeRecord(this);
    }

    public void writeInstance(Instance instance) {
        instance.write(this);
    }

    public void writeString(String string) {
        var bytes = string.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length);
        write(bytes);
    }

    public void writeDouble(double d) {
        long l = Double.doubleToRawLongBits(d);
        for (int s = 0; s < 64; s += 8)
            write((int) (l >> s & 0xff));
    }

    public void writeBoolean(boolean bool) {
        write(bool ? 1 : 0);
    }

    public void writeInt(int i) {
        writeLong(i);
    }

    public void writeId(Id id) {
        id.write(this);
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

    public void write(byte @NotNull [] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new InternalException("Failed to write to the underlying output stream", e);
        }
    }

    public void write(int b) {
        try {
            outputStream.write(b);
        } catch (IOException e) {
            throw new InternalException("Failed to write to the underlying output stream", e);
        }
    }

    public void writeIdTag(IdTag idTag, boolean isArray) {
        write(idTag.maskedCode(isArray));
    }

}
