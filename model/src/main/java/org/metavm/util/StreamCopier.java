package org.metavm.util;

import org.metavm.entity.TreeTags;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeOrTypeKey;

import java.io.InputStream;
import java.io.OutputStream;

public class StreamCopier extends StreamVisitor {

    protected final InstanceOutput output;

    public StreamCopier(InputStream in, OutputStream out) {
        this(new InstanceInput(in), new InstanceOutput(out));
    }
    
    public StreamCopier(InstanceInput input, InstanceOutput output) {
        super(input);
        this.output = output;
    }

    @Override
    public void visitGrove() {
        int numTrees = readInt();
        output.writeInt(numTrees);
        for (int i = 0; i < numTrees; i++) {
            visitTree();
        }
    }

    @Override
    public void visitTree() {
        var treeTag = read();
        output.write(treeTag);
        switch (treeTag) {
            case TreeTags.DEFAULT -> visitMessage();
            case TreeTags.MIGRATED -> visitForwardingPointer();
            default -> throw new IllegalStateException("Invalid tree tag: " + treeTag);
        }
    }

    @Override
    public void visitForwardingPointer() {
        output.writeId(readId());
        output.writeId(readId());
    }

    @Override
    public void visitMessage() {
        visitVersion(readLong());
        output.writeLong(readTreeId());
        output.writeLong(readLong());
        boolean separateChild = readBoolean();
        if(separateChild) {
            output.writeBoolean(true);
            output.writeId(readId());
            output.writeId(readId());
        }
        else
            output.writeBoolean(false);
        visit();
    }

    @Override
    public void visitVersion(long version) {
        output.writeLong(version);
    }

    @Override
    public void visitField() {
        output.writeLong(readLong());
        visit();
    }

    @Override
    public void visitRecord() {
        output.write(WireTypes.RECORD);
        var nodeId = readLong();
        output.writeLong(nodeId);
        visitRecord(-1L, -1L, false, getTreeId(), nodeId);
    }

    @Override
    public void visitMigratingRecord() {
        output.write(WireTypes.MIGRATING_RECORD);
        var oldTreeId = readLong();
        output.writeLong(oldTreeId);
        var oldNodeId = readLong();
        output.writeLong(oldNodeId);
        var useOldId = readBoolean();
        output.writeBoolean(useOldId);
        var nodeId = readLong();
        output.writeLong(nodeId);
        visitRecord(oldTreeId, oldNodeId, useOldId, getTreeId(), nodeId);
    }

    @Override
    public void visitRecord(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId) {
        var typeKey = readTypeKey();
        typeKey.write(output);
        visitRecordBody(oldTreeId, oldNodeId, useOldId, treeId, nodeId, typeKey);
    }


    @Override
    public void visitValue() {
        output.write(WireTypes.VALUE);
        var typeKey = readTypeKey();
        typeKey.write(output);
        visitBody(typeKey);
    }

    @Override
    public void visitBody(TypeOrTypeKey typeOrTypeKey) {
        if (typeOrTypeKey.isArray()) {
            int len = readInt();
            output.writeInt(len);
            for (int i = 0; i < len; i++) {
                visit();
            }
        } else {
            int numKlasses = readInt();
            output.writeInt(numKlasses);
            for (int i = 0; i < numKlasses; i++) {
                output.writeLong(readLong());
                int numFields = readInt();
                output.writeInt(numFields);
                for (int j = 0; j < numFields; j++) {
                    visitField();
                }
            }
        }
    }

    @Override
    public void visitReference() {
        output.write(WireTypes.REFERENCE);
        output.writeId(readId());
    }

    @Override
    public void visitRedirectingRecord() {
        output.write(WireTypes.REDIRECTING_RECORD);
        visit();
        output.writeId(readId());
        visit();
    }

    @Override
    public void visitRedirectingReference() {
        output.write(WireTypes.REDIRECTING_REFERENCE);
        output.write(read());
        output.writeId(readId());
        visit();
        output.writeId(readId());
    }

    @Override
    protected void visitFlaggedReference() {
        output.write(WireTypes.FLAGGED_REFERENCE);
        var flags = read();
        output.write(flags);
        output.writeId(readId());
    }

    @Override
    public void visitString() {
        output.write(WireTypes.STRING);
        output.writeString(readString());
    }

    @Override
    public void visitLong() {
        output.write(WireTypes.LONG);
        output.writeLong(readLong());
    }

    @Override
    public void visitDouble() {
        output.write(WireTypes.DOUBLE);
        output.writeDouble(readDouble());
    }

    @Override
    public void visitBoolean() {
        output.write(WireTypes.BOOLEAN);
        output.writeBoolean(readBoolean());
    }

    @Override
    public void visitPassword() {
        output.write(WireTypes.PASSWORD);
        output.writeString(readString());
    }

    @Override
    public void visitTime() {
        output.write(WireTypes.TIME);
        output.writeLong(readLong());
    }

    public void writeLong(long l) {
        output.writeLong(l);
    }

    public void write(int b) {
        output.write(b);
    }

    public void writeId(Id id) {
        output.writeId(id);
    }

    @Override
    public void visitNull() {
        output.write(WireTypes.NULL);
    }

    public InstanceOutput getOutput() {
        return output;
    }
}
