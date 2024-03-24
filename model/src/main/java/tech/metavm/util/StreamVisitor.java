package tech.metavm.util;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TypeTag;
import tech.metavm.system.RegionConstants;

import java.io.InputStream;

public class StreamVisitor {

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
            case WireTypes.RECORD -> visitRecord();
            default -> throw new InternalException("Invalid wire type: " + wireType);
        }
    }

    public InstanceInput getInput() {
        return input;
    }

    public void visitMessage() {
        input.readLong();
        input.readInt();
        visit();
    }

    public void visit() {
        visit(input.read());
    }

    public void visitField() {
        input.readId();
        visit();
    }

    public void visitRecord() {
        visitRecordBody(readId());
    }

    public Id readId() {
        return input.readId();
    }

    public TypeTag readTypeTag() {
        return TypeTag.fromCode(input.read());
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

    public int read() {
        return input.read();
    }

    public String readString() {
        return input.readString();
    }

    public boolean readBoolean() {
        return input.readBoolean();
    }

    public void visitRecordBody(Id id) {
        if (RegionConstants.isArrayId(id)) {
            int len = input.readInt();
            for (int i = 0; i < len; i++)
                visit();
        } else {
            int numFields = input.readInt();
            for (int i = 0; i < numFields; i++)
                visitField();
        }
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
}
