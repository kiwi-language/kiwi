package org.metavm.util;

import org.metavm.entity.TreeTags;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.TypeOrTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class StreamVisitor {

    private static final Logger logger = LoggerFactory.getLogger(StreamVisitor.class);
    private final InstanceInput input;

    public StreamVisitor(InputStream in) {
        this(new InstanceInput(in));
    }

    public StreamVisitor(InstanceInput input) {
        this.input = input;
    }

    protected void visit(int wireType) {
        switch (wireType) {
            case WireTypes.NULL -> visitNull();
            case WireTypes.DOUBLE -> visitDouble();
            case WireTypes.STRING -> visitString();
            case WireTypes.LONG -> visitLong();
            case WireTypes.BOOLEAN -> visitBoolean();
            case WireTypes.TIME -> visitTime();
            case WireTypes.PASSWORD -> visitPassword();
            case WireTypes.REFERENCE -> visitReference();
            case WireTypes.FLAGGED_REFERENCE -> visitFlaggedReference();
            case WireTypes.REDIRECTING_REFERENCE -> visitRedirectingReference();
            case WireTypes.REDIRECTING_RECORD -> visitRedirectingRecord();
            case WireTypes.RECORD -> visitRecord();
            case WireTypes.MIGRATING_RECORD -> visitMigratingRecord();
            case WireTypes.VALUE -> visitValue();
            default -> throw new InternalException("Invalid wire type: " + wireType);
        }
    }

    public void visitRedirectingReference() {
        read();
        visitReference();
        visit();
        readId();
    }

    public void visitRedirectingRecord() {
        visit();
        readId();
        visit();
        readId();
    }

    protected void visitFlaggedReference() {
        read();
        visitReference();
    }

    public InstanceInput getInput() {
        return input;
    }

    public void visitGrove() {
        int trees = readInt();
        for (int i = 0; i < trees; i++) {
            visitTree();;
        }
    }

    public void visitTree() {
        var treeTag = read();
        switch (treeTag) {
            case TreeTags.DEFAULT -> visitMessage();
            case TreeTags.MIGRATED -> visitForwardingPointer();
            default -> throw new IllegalStateException("Invalid tree tag: " + treeTag);
        }
    }

    public void visitForwardingPointer() {
        readId();
        readId();
    }

    public void visitMessage() {
        visitVersion(input.readLong());
        readTreeId();
        input.readLong();
        boolean separateChild = readBoolean();
        if(separateChild) {
            readId();
            readId();
        }
        visit();
    }

    public void visitVersion(long version) {}

    public void visit() {
        visit(input.read());
    }

    public void visitField() {
        input.readLong();
        visit();
    }

    public void visitRecord() {
        visitRecord(-1L, -1L, false, getTreeId(), readLong());
    }

    public void visitMigratingRecord() {
        visitRecord(readLong(), readLong(), readBoolean(), getTreeId(), readLong());
    }

    public void visitRecord(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId) {
        visitRecordBody(oldTreeId, oldNodeId, useOldId, treeId, nodeId, TypeKey.read(input));
    }

    public void visitValue() {
        visitBody(TypeKey.read(input));
    }

    public Id readId() {
        return input.readId();
    }

    public int readInt() {
        return input.readInt();
    }

    public double readDouble() {
        return input.readDouble();
    }

    public long readLong() {
        return input.readLong();
    }

    public Type readType(TypeDefProvider typeDefProvider) {
        return Type.readType(input, typeDefProvider);
    }

    public int read() {
        return input.read();
    }

    public String readString() {
        return input.readString();
    }

    public boolean readBoolean() {
        return input.readBoolean();
    }

    public void visitRecordBody(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey) {
        visitBody(typeOrTypeKey);
    }

    public void visitBody(TypeOrTypeKey typeOrTypeKey) {
        if (typeOrTypeKey.isArray()) {
            int len = input.readInt();
            for (int i = 0; i < len; i++)
                visit();
        } else {
            int numKlasses = input.readInt();
            for (int i = 0; i < numKlasses; i++) {
                input.readLong();
                int numFields = input.readInt();
                for (int j = 0; j < numFields; j++)
                    visitField();
            }
        }
    }

    public TypeKey readTypeKey() {
        return TypeKey.read(input);
    }

    public void visitReference() {
        input.readId();
    }

    public void visitString() {
        input.readString();
    }

    public void visitLong() {
        input.readLong();
    }

    public void visitDouble() {
        input.readDouble();
    }

    public void visitBoolean() {
        input.readBoolean();
    }

    public void visitPassword() {
        input.readString();
    }

    public void visitTime() {
        input.readLong();
    }

    public void visitNull() {
    }

    public long readTreeId() {
        return input.readTreeId();
    }

    public long getTreeId() {
        return input.getTreeId();
    }

}
