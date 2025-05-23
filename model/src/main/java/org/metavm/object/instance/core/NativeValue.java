package org.metavm.object.instance.core;

import org.metavm.util.MvOutput;

import java.util.function.Consumer;

public interface NativeValue extends Value {

    @Override
    default String getTitle() {
        return getClass().getSimpleName();
    }

    @Override
    default void writeInstance(MvOutput output) {
        write(output);
    }

    @Override
    void write(MvOutput output);

    @Override
    default Object toSearchConditionValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    default <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    default Object toJson() {
        return null;
    }

    @Override
    default void writeTree(TreeWriter treeWriter) {
        treeWriter.write(getTitle());
    }

    void forEachReference(Consumer<Reference> action);
}
