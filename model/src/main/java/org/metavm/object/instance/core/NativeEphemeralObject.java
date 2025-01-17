package org.metavm.object.instance.core;

import org.metavm.entity.NativeObject;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.HashMap;
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
    default InstanceParam getParam() {
        throw new UnsupportedOperationException();
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

    default Map<String, Object> toJson() {
        var map = new HashMap<String, Object>();
        buildJson(map);
        return map;
    }

    void buildJson(Map<String, Object> map);

    @Override
    default Map<String, Value> buildSource() {
        throw new UnsupportedOperationException();
    }
}
