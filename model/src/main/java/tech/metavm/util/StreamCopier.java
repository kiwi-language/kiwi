package tech.metavm.util;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.system.RegionConstants;

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
    public void visitMessage() {
        output.writeLong(readLong());
        output.writeInt(readInt());
        visit();
    }

    @Override
    public void visitField() {
        output.writeId(readId());
        visit();
    }

    @Override
    public void visitRecord() {
        output.write(WireTypes.RECORD);
        var id = readId();
        output.writeId(id);
        visitRecordBody(id);
    }

    @Override
    public void visitRecordBody(Id id) {
        if (RegionConstants.isArrayId(id)) {
            int len = readInt();
            output.writeInt(len);
            for (int i = 0; i < len; i++) {
                visit();
            }
        } else {
            int numFields = readInt();
            output.writeInt(numFields);
            for (int i = 0; i < numFields; i++) {
                visitField();
            }
        }
    }

    @Override
    public void visitReference() {
        output.write(WireTypes.REFERENCE);
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
}
