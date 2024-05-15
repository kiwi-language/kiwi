package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.core.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public class InstanceOutput extends OutputStream {

    public static byte[] toByteArray(DurableInstance instance) {
        return toByteArray(instance, false, false);
    }

    public static byte[] toMessage(DurableInstance instance) {
        NncUtils.requireTrue(instance.isRoot());
        return toByteArray(instance, true, true);
    }

    public static byte[] toByteArray(DurableInstance instance, boolean includeChild, boolean withHeader) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout, includeChild);
        if (withHeader) {
            output.writeLong(instance.getVersion());
            output.writeLong(instance.getTreeId());
            output.writeInt(instance.getNextNodeId());
        }
        output.writeValue(instance);
        return bout.toByteArray();
    }

    private final OutputStream outputStream;
    private final boolean includeChildren;

    public InstanceOutput(OutputStream outputStream) {
        this(outputStream, false);
    }

    public InstanceOutput(OutputStream outputStream, boolean includeChildren) {
        this.outputStream = outputStream;
        this.includeChildren = includeChildren;
    }

    public void writeValue(Instance instance) {
        writeInstance0(instance, false);
    }

    public void writeInstance(Instance instance) {
        writeInstance0(instance, true);
    }

    private void writeInstance0(Instance instance, boolean isReference) {
        if (instance instanceof PrimitiveInstance primitiveInstance) {
            write(primitiveInstance.getWireType());
            primitiveInstance.writeTo(this, includeChildren);
        } else if (instance instanceof DurableInstance d) {
            /*if (d.isEphemeral())
                write(WireTypes.NULL);
            else*/
            if (isReference) {
                write(WireTypes.REFERENCE);
                writeId(d.getId());
            } else {
                write(WireTypes.RECORD);
                var id = (PhysicalId) d.getId();
                id.writeWithoutTreeId(this);
                d.writeTo(this, includeChildren);
            }
        } else
            throw new InternalException("Invalid instance: " + instance);
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
