package org.metavm.object.instance.core;

import org.metavm.entity.NativeObject;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

public interface NativeEphemeralObject extends NativeObject {

    @Override
    default boolean isEphemeral() {
        return true;
    }

    @Nullable
    @Override
    default Id tryGetId() {
        return null;
    }

    @Override
    default String getTitle() {
        return toString();
    }


    @Override
    default Id getId() {
        return null;
    }

    @Override
    default void writeTree(TreeWriter treeWriter) {
        treeWriter.write(toString());
    }

    @Override
    default void forEachValue(Consumer<? super Instance> action) {}

    @Override
    default void write(MvOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void writeTo(MvOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitNativeEphemeralObject(this);
    }

    @Override
    default Instance getRoot() {
        return this;
    }

    @Override
    default boolean isRoot() {
        return true;
    }

    @Nullable
    @Override
    default Instance getParent() {
        return null;
    }

    @Override
    default Map<String, Value> buildSource() {
        throw new UnsupportedOperationException();
    }

    @Override
    default String getText() {
        return toString();
    }

    @Override
    default void incRefcount(int amount) {}

    @Override
    default int getRefcount() {
        return 0;
    }

    @Override
    default Klass getInstanceKlass() {
        throw new UnsupportedOperationException();
    }

    @Override
    default ClassType getInstanceType() {
        throw new UnsupportedOperationException();
    }
}
