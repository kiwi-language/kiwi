package tech.metavm.util;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.object.type.TypeOrTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;

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
        readTreeId();
        input.readInt();
        visit();
    }

    public void visit() {
        visit(input.read());
    }

    public void visitField() {
        input.readLong();
        visit();
    }

    public void visitRecord() {
        visitRecordBody(readLong(), TypeKey.read(input));
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

    public void visitRecordBody(long nodeId, TypeOrTypeKey typeOrTypeKey) {
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
