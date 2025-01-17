package org.metavm.object.instance.core;

import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface NativeValue extends Value {

    @Nullable
    @Override
    default Id tryGetId() {
        return null;
    }

    @Override
    default FieldValue toFieldValueDTO() {
        throw new UnsupportedOperationException();
    }

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
    default InstanceParam getParam() {
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
