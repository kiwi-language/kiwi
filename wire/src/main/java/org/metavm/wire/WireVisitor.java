package org.metavm.wire;

public interface WireVisitor {

    void visitBoolean();

    void visitChar();

    int visitByte();

    void visitShort();

    int visitInt();

    void visitLong();

    void visitFloat();

    void visitDouble();

    void visitString();

    void visitDate();

    void visitBytes();

    void visitEntity(WireAdapter<?> adapter);

    default void visitEntity() {
        visitEntity(AdapterRegistry.instance.getObjectAdapter());
    }

    void visitNullable(Runnable visit);

    void visitList(Runnable visitElement);

    void visitArray(Runnable visitElement);

    WireInput getInput();

}
