package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.Entity;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.KlassDataSlot;
import org.metavm.object.instance.core.Message;
import org.metavm.object.instance.log.InstanceLog;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.ReferencePO;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;

@Slf4j
public class InstanceOutput extends MvOutput {

    public static byte[] toBytes(List<Message> messages) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeInt(messages.size());
        messages.forEach(msg -> msg.writeTo(output));
        return bout.toByteArray();
    }

    public static byte[] toBytes(Instance instance) {
        var bout = new ByteArrayOutputStream();
        var output = new InstanceOutput(bout);
        output.writeInt(1);
        instance.writeTo(output);
        return bout.toByteArray();
    }

    private @Nullable Instance current;
    private @Nullable KlassDataSlot currentKlassSlot;

    public InstanceOutput(OutputStream outputStream) {
        super(outputStream);
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
        writeInt(indexKeyPO.getData().length);
        write(indexKeyPO.getData());
    }

    public void writeReferencePO(ReferencePO referencePO) {
        writeLong(referencePO.getSourceTreeId());
        write(referencePO.getTargetId());
        writeInt(referencePO.getKind());
    }

    public @javax.annotation.Nullable Instance getCurrent() {
        return current;
    }

    public void setCurrent(@javax.annotation.Nullable Instance current) {
        this.current = current;
    }

    @javax.annotation.Nullable
    public KlassDataSlot getCurrentKlassSlot() {
        return currentKlassSlot;
    }

    public void setCurrentKlassSlot(@javax.annotation.Nullable KlassDataSlot currentKlassSlot) {
        this.currentKlassSlot = currentKlassSlot;
    }

    @Override
    public void writeEntity(Entity entity) {
        write(entity.getEntityTag());
        super.writeEntity(entity);
    }
}
