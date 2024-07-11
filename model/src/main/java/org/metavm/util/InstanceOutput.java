package org.metavm.util;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.TreeTags;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.IdTag;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.ReferencePO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public class InstanceOutput extends OutputStream {

    private static final Logger logger = LoggerFactory.getLogger(InstanceOutput.class);

    public static byte[] toBytes(DurableInstance instance) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeMessage(instance);
        return bout.toByteArray();
    }

    public static byte[] toMigrationsBytes(DurableInstance instance) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        instance.writeForwardingPointers(output);
        return bout.toByteArray();
    }

    private final OutputStream outputStream;

    public InstanceOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeMessage(DurableInstance instance) {
        write(TreeTags.DEFAULT);
        // !!! IMPORTANT: Version must starts at the second byte. @see org.metavm.entity.ContextDifference.incVersion !!!
        writeLong(instance.getVersion());
        writeLong(instance.getTreeId());
        writeLong(instance.getNextNodeId());
        if(instance.isSeparateChild()) {
            writeBoolean(true);
            writeId(Objects.requireNonNull(instance.getParent()).getId());
            writeId(Objects.requireNonNull(instance.getParentField()).getId());
        }
        else
            writeBoolean(false);
        writeRecord(instance.getReference());
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

    public <T> void writeList(Collection<? extends T> list, Consumer<T> write) {
        writeInt(list.size());
        list.forEach(write);
    }

    public void writeInstancePO(InstancePO instancePO) {
        writeLong(instancePO.getId());
        writeInt(instancePO.getData().length);
        write(instancePO.getData());
        writeLong(instancePO.getNextNodeId());
    }

    public void writeIndexEntryPO(IndexEntryPO entry) {
        writeIndexKeyPO(entry.getKey());
        write(entry.getInstanceId());
    }

    public void writeIndexKeyPO(IndexKeyPO indexKeyPO) {
        write(indexKeyPO.getIndexId());
        for (int i = 0; i < IndexKeyPO.MAX_KEY_COLUMNS; i++) {
            var col = indexKeyPO.getColumn(i);
            writeInt(col.length);
            write(col);
        }
    }

    public void writeReferencePO(ReferencePO referencePO) {
        writeLong(referencePO.getSourceTreeId());
        write(referencePO.getTargetId());
        writeInt(referencePO.getKind());
    }

    public void writeInstanceLog(InstanceLog instanceLog) {
        writeId(instanceLog.getId());
        writeInt(instanceLog.getChangeType().ordinal());
        writeLong(instanceLog.getVersion());
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
