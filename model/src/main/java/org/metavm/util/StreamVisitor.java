package org.metavm.util;

import org.metavm.entity.EntityRegistry;
import org.metavm.entity.TreeTags;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
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

    protected void visitValue(int wireType) {
        switch (wireType) {
            case WireTypes.NULL -> visitNull();
            case WireTypes.DOUBLE -> visitDouble();
            case WireTypes.FLOAT -> visitFloat();
            case WireTypes.STRING -> visitUTF();
            case WireTypes.LONG -> visitLong();
            case WireTypes.INT -> visitInt();
            case WireTypes.BOOLEAN -> visitBoolean();
            case WireTypes.CHAR -> visitChar();
            case WireTypes.SHORT -> visitShort();
            case WireTypes.BYTE -> visitByte();
            case WireTypes.TIME -> visitTime();
            case WireTypes.PASSWORD -> visitPassword();
            case WireTypes.REFERENCE -> visitReference();
            case WireTypes.FLAGGED_REFERENCE -> visitFlaggedReference();
            case WireTypes.REDIRECTING_REFERENCE -> visitRedirectingReference();
            case WireTypes.REDIRECTING_INSTANCE -> visitRedirectingInstance();
            case WireTypes.INSTANCE -> visitInstance();
            case WireTypes.RELOCATING_INSTANCE -> visitRelocatingInstance();
            case WireTypes.VALUE_INSTANCE -> visitValueInstance();
            case WireTypes.REMOVING_INSTANCE -> visitRemovingInstance();
            case WireTypes.CLASS_TYPE -> visitClassType();
            case WireTypes.PARAMETERIZED_TYPE -> visitParameterizedType();
            case WireTypes.VARIABLE_TYPE -> visitVariableType();
            case WireTypes.CAPTURED_TYPE -> visitCapturedType();
            case WireTypes.LONG_TYPE -> visitLongType();
            case WireTypes.INT_TYPE -> visitIntType();
            case WireTypes.CHAR_TYPE -> visitCharType();
            case WireTypes.SHORT_TYPE -> visitShortType();
            case WireTypes.BYTE_TYPE -> visitByteType();
            case WireTypes.DOUBLE_TYPE -> visitDoubleType();
            case WireTypes.FLOAT_TYPE -> visitFloatType();
            case WireTypes.NULL_TYPE -> visitNullType();
            case WireTypes.VOID_TYPE -> visitVoidType();
            case WireTypes.TIME_TYPE -> visitTimeType();
            case WireTypes.PASSWORD_TYPE -> visitPasswordType();
            case WireTypes.STRING_TYPE -> visitStringType();
            case WireTypes.BOOLEAN_TYPE -> visitBooleanType();
            case WireTypes.FUNCTION_TYPE -> visitFunctionType();
            case WireTypes.UNCERTAIN_TYPE -> visitUncertainType();
            case WireTypes.UNION_TYPE -> visitUnionType();
            case WireTypes.INTERSECTION_TYPE -> visitIntersectionType();
            case WireTypes.READ_ONLY_ARRAY_TYPE -> visitReadOnlyArrayType();
            case WireTypes.ARRAY_TYPE -> visitArrayType();
            case WireTypes.NEVER_TYPE -> visitNeverType();
            case WireTypes.ANY_TYPE -> visitAnyType();
            case WireTypes.FIELD_REF -> visitFieldRef();
            case WireTypes.METHOD_REF -> visitMethodRef();
            case WireTypes.FUNCTION_REF -> visitFunctionRef();
            case WireTypes.INDEX_REF -> visitIndexRef();
            case WireTypes.LAMBDA_REF -> visitLambdaRef();
            default -> throw new InternalException("Invalid wire type: " + wireType);
        }
    }

    public void visitFieldRef() {
        visitValue();
        readId();
    }

    public void visitMethodRef() {
        visitValue();
        readId();
        var typeArgCnt = readInt();
        for (int i = 0; i < typeArgCnt; i++) {
            visitValue();
        }
    }

    public void visitFunctionRef() {
        readId();
        var typeArgCnt = readInt();
        for (int i = 0; i < typeArgCnt; i++) {
            visitValue();
        }
    }

    public void visitIndexRef() {
        visitValue();
        readId();
    }

    public void visitLambdaRef() {
        visitValue();
        readId();
    }

    public void visitClassType() {
        visitReference();
    }

    public void visitParameterizedType() {
        visitValue();
        visitReference();
        int typeArgCnt = readInt();
        for (int i = 0; i < typeArgCnt; i++) {
            visitValue();
        }
    }

    public void visitVariableType() {
        visitReference();
    }

    public void visitCapturedType() {
        visitReference();
    }

    public void visitLongType() {
    }

    public void visitIntType() {
    }

    public void visitCharType() {
    }

    public void visitShortType() {
    }

    public void visitByteType() {
    }

    public void visitDoubleType() {
    }

    public void visitFloatType() {
    }

    public void visitNullType() {
    }

    public void visitVoidType() {
    }

    public void visitTimeType() {
    }

    public void visitPasswordType() {
    }

    public void visitStringType() {
    }

    public void visitBooleanType() {
    }

    public void visitFunctionType() {
        int paramCnt = readInt();
        for (int i = 0; i < paramCnt; i++) {
            visitValue();
        }
        visitValue();
    }

    public void visitUncertainType() {
        visitValue();
        visitValue();
    }

    public void visitUnionType() {
        int memberCnt = readInt();
        for (int i = 0; i < memberCnt; i++) {
            visitValue();
        }
    }

    public void visitIntersectionType() {
        int memberCnt = readInt();
        for (int i = 0; i < memberCnt; i++) {
            visitValue();
        }
    }

    public void visitReadOnlyArrayType() {
        visitValue();
    }

    public void visitArrayType() {
        visitValue();
    }

    public void visitNeverType() {

    }

    public void visitAnyType() {
    }

    public void visitShort() {
        input.readShort();
    }

    public int visitByte() {
        return input.read();
    }

    protected int readShort() {
        return input.readShort();
    }

    protected void visitRemovingInstance() {
        visitInstance();
    }

    public void visitRedirectingReference() {
        read();
        visitReference();
        visitValue();
        readId();
    }

    public void visitRedirectingInstance() {
        visitValue();
        readId();
        visitValue();
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
        visitTree(read());
    }

    public void visitTree(int treeTag) {
        switch (treeTag) {
            case TreeTags.DEFAULT -> visitMessage();
            case TreeTags.RELOCATED -> visitForwardingPointer();
            case TreeTags.ENTITY -> visitEntityMessage();
            default -> throw new IllegalStateException("Unrecognized tree tag: " + treeTag);
        }
    }

    public void visitForwardingPointer() {
        readId();
        readId();
    }

    public void visitMessage() {
        visitVersion(input.readLong());
        readTreeId();
        visitNextNodeId(input.readLong());
        boolean separateChild = readBoolean();
        if(separateChild) {
            readId();
            readId();
        }
        visitValue();
    }

    public void visitEntityMessage() {
        readTreeId();
        visitNextNodeId(input.readLong());
        visitEntity();
    }

    public void visitVersion(long version) {}

    public void visitNextNodeId(long nextNodeId) {}

    public void visitValue() {
        visitValue(input.read());
    }

    public void visitField() {
        input.readLong();
        visitValue();
    }

    public void visitInstance() {
        visitInstance(-1L, -1L, false, getTreeId(), readLong());
    }

    public void visitRelocatingInstance() {
        visitInstance(readLong(), readLong(), readBoolean(), getTreeId(), readLong());
    }

    public void visitInstance(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId) {
        visitInstanceBody(oldTreeId, oldNodeId, useOldId, treeId, nodeId, TypeKey.read(input));
    }

    public void visitValueInstance() {
        visitBody(TypeKey.read(input));
    }

    public Id readId() {
        return input.readId();
    }

    public int readInt() {
        return input.readInt();
    }

    public MarkingInstanceOutput.Block readBlock() {
        return MarkingInstanceOutput.Block.read(input);
    }

    public double readDouble() {
        return input.readDouble();
    }

    public float readFloat() {
        return input.readFloat();
    }

    public long readLong() {
        return input.readLong();
    }

    public char readChar() {
        return input.readChar();
    }

    public Type readType() {
        return input.readType();
    }

    public int read() {
        return input.read();
    }

    public void read(byte[] buf) {
        input.read(buf);
    }

    public String readUTF() {
        return input.readUTF();
    }

    public byte[] readBytes() {
        return input.readBytes();
    }

    public boolean readBoolean() {
        return input.readBoolean();
    }

    public void visitInstanceBody(long oldTreeId, long oldNodeId, boolean useOldId, long treeId, long nodeId, TypeOrTypeKey typeOrTypeKey) {
        visitBody(typeOrTypeKey);
    }

    public void visitBody(TypeOrTypeKey typeOrTypeKey) {
        if (typeOrTypeKey.isArray()) {
            int len = input.readInt();
            for (int i = 0; i < len; i++)
                visitValue();
        } else {
            int numKlasses = input.readInt();
            for (int i = 0; i < numKlasses; i++) {
                input.readLong();
                visitClassBody();
            }
            int childrenCount = input.readInt();
            for (int i = 0; i < childrenCount; i++) {
                visitValue();
            }
        }
    }

    public void visitClassBody() {
        int numFields = input.readInt();
        if(numFields == -1)
            visitCustomData();
        else {
            for (int j = 0; j < numFields; j++)
                visitField();
        }
    }

    public void visitCustomData() {
        var numBlocks = input.readInt();
        var blocks = new MarkingInstanceOutput.Block[numBlocks];
        for (int i = 0; i < numBlocks; i++) {
            blocks[i] = MarkingInstanceOutput.Block.read(input);
        }
        for (var block : blocks) {
           block.visitBody(this);
        }
    }

    public TypeKey readTypeKey() {
        return TypeKey.read(input);
    }

    public void visitReference() {
        input.readId();
    }

    public String visitUTF() {
        return input.readUTF();
    }

    public void visitBytes() {
        input.readBytes();
    }

    public void visitLong() {
        input.readLong();
    }

    public void visitInt() {
        input.readInt();
    }

    public void visitDouble() {
        input.readDouble();
    }

    public void visitFloat() {
        input.readFloat();
    }

    public void visitBoolean() {
        input.readBoolean();
    }

    public void visitChar() {
        input.readChar();
    }

    public void visitPassword() {
        input.readUTF();
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

    public void visitBytes(int length) {
        input.skip(length);
    }

    public void visitNullable(Runnable visit) {
        if (readBoolean())
            visit.run();
    }

    public void visitList(Runnable visit) {
        var size = readInt();
        for (int i = 0; i < size; i++) {
            visit.run();
        }
    }

    public void visitId() {
        input.readId();
    }

    public void visitEntity() {
        visitEntityBody(read(), readId());
    }

    public void visitEntityBody(int tag, Id id) {
        var h = EntityRegistry.getVisitBodyHandle(tag);
        try {
            h.invokeExact(this);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
