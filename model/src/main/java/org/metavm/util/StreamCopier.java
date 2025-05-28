package org.metavm.util;

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
        visitTree(treeTag);
    }

    @Override
    public void visitValue() {
        var tag = read();
        write(tag);
        visitValue(tag);
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
        visitValue();
    }

    @Override
    public void visitEntityMessage() {
        output.writeLong(readLong());
        output.writeLong(readTreeId());
        output.writeLong(readLong());
        visitEntity();
    }

    @Override
    public void visitVersion(long version) {
        output.writeLong(version);
    }

    @Override
    public void visitField() {
        output.writeLong(readLong());
        visitValue();
    }

    @Override
    protected void visitRemovingInstance() {
        copyInstance();
    }

    @Override
    public void visitInstance() {
        copyInstance();
    }

    private void copyInstance() {
        var nodeId = readLong();
        output.writeLong(nodeId);
        visitInstance(getTreeId(), nodeId);
    }

    @Override
    public void visitInstance(long treeId, long nodeId) {
        var typeKey = readTypeKey();
        typeKey.write(output);
        var refcount = readInt();
        output.writeInt(refcount);
        visitInstanceBody(treeId, nodeId, typeKey, refcount);
    }

    @Override
    public void visitEntity() {
        var tag = read();
        var id = readId();
        var refcount = readInt();
        output.write(tag);
        output.writeId(id);
        output.writeInt(refcount);
        visitEntityBody(tag, id, refcount);
    }

    @Override
    public void visitValueInstance() {
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
                visitValue();
            }
        } else {
            int numKlasses = readInt();
            output.writeInt(numKlasses);
            for (int i = 0; i < numKlasses; i++) {
                output.writeLong(readLong());
                visitClassBody();
            }
            int childrenCount = readInt();
            output.writeInt(childrenCount);
            for (int i = 0; i < childrenCount; i++) {
                visitValue();
            }
        }
    }

    @Override
    public void visitClassBody() {
        int numFields = readInt();
        output.writeInt(numFields);
        if(numFields == -1)
            visitCustomData();
        else {
            for (int j = 0; j < numFields; j++) {
                visitField();
            }
        }
    }

    @Override
    public void visitCustomData() {
        var numBlocks = readInt();
        output.writeInt(numBlocks);
        var blocks = new MarkingInstanceOutput.Block[numBlocks];
        for (int i = 0; i < numBlocks; i++) {
            (blocks[i] = readBlock()).write(output);
        }
        for (var block : blocks) {
            block.visitBody(this);
        }
    }

    @Override
    public void visitBytes(int len) {
        var buf = new byte[len];
        read(buf);
        output.write(buf);
    }

    @Override
    public void visitReference() {
        output.writeId(readId());
    }

    @Override
    public void visitRedirectingReference() {
        output.write(read());
        output.writeId(readId());
        visitValue();
        output.writeId(readId());
    }

    @Override
    public void visitRedirectingInstance() {
        visitValue();
        output.writeId(readId());
        visitValue();
    }

    @Override
    public String visitUTF() {
        var utf = readUTF();
        output.writeUTF(utf);
        return utf;
    }

    @Override
    public void visitBytes() {
        output.writeBytes(readBytes());
    }

    @Override
    public void visitLong() {
        output.writeLong(readLong());
    }

    @Override
    public void visitInt() {
        output.writeLong(readInt());
    }

    @Override
    public void visitChar() {
        output.writeChar(readChar());
    }

    @Override
    public void visitShort() {
        output.writeShort(readShort());
    }

    @Override
    public int visitByte() {
        var b = read();
        output.write(b);
        return b;
    }

    @Override
    public void visitDouble() {
        output.writeDouble(readDouble());
    }

    @Override
    public void visitFloat() {
        output.writeFloat(readFloat());
    }

    @Override
    public void visitBoolean() {
        output.writeBoolean(readBoolean());
    }

    @Override
    public void visitPassword() {
        output.writeUTF(readUTF());
    }

    @Override
    public void visitTime() {
        output.writeLong(readLong());
    }

    @Override
    public void visitParameterizedType() {
        visitValue();
        visitReference();
        int typeArgCnt = readInt();
        output.writeInt(typeArgCnt);
        for (int i = 0; i < typeArgCnt; i++) {
            visitValue();
        }
    }

    @Override
	public void visitFunctionType() {
        int paramCnt = readInt();
        output.writeInt(paramCnt);
        for (int i = 0; i < paramCnt; i++) {
            visitValue();
        }
        visitValue();
    }

    @Override
	public void visitUncertainType() {
        visitValue();
        visitValue();
    }

    @Override
	public void visitUnionType() {
        int memberCnt = readInt();
        output.writeInt(memberCnt);
        for (int i = 0; i < memberCnt; i++) {
            visitValue();
        }
    }

    @Override
	public void visitIntersectionType() {
        int memberCnt = readInt();
        output.writeInt(memberCnt);
        for (int i = 0; i < memberCnt; i++) {
            visitValue();
        }
    }

    @Override
	public void visitReadOnlyArrayType() {
        visitValue();
    }

    @Override
	public void visitArrayType() {
        visitValue();
    }

    @Override
    public void visitFieldRef() {
        visitValue();
        output.writeId(readId());
    }

    @Override
    public void visitMethodRef() {
        visitValue();
        output.writeId(readId());
        var typeArgCnt = readInt();
        output.writeInt(typeArgCnt);
        for (int i = 0; i < typeArgCnt; i++) {
            visitValue();
        }
    }

    @Override
    public void visitFunctionRef() {
        output.writeId(readId());
        var typeArgCnt = readInt();
        output.writeInt(typeArgCnt);
        for (int i = 0; i < typeArgCnt; i++) {
            visitValue();
        }
    }

    @Override
    public void visitIndexRef() {
        visitValue();
        output.writeId(readId());
    }

    @Override
    public void visitLambdaRef() {
        visitValue();
        output.writeId(readId());
    }

    public void writeLong(long l) {
        output.writeLong(l);
    }

    public void write(int b) {
        output.write(b);
    }

    public void writeInt(int i) {
        output.writeInt(i);
    }

    public void writeId(Id id) {
        output.writeId(id);
    }

    @Override
    public void visitNullable(Runnable visit) {
        var notNull = readBoolean();
        output.writeBoolean(notNull);
        if (notNull)
            visit.run();
    }

    @Override
    public void visitList(Runnable visit) {
        var size = readInt();
        output.writeInt(size);
        for (int i = 0; i < size; i++) {
            visit.run();
        }
    }

    @Override
    public void visitId() {
        output.writeId(readId());
    }

    public InstanceOutput getOutput() {
        return output;
    }
}
