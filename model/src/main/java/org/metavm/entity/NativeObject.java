package org.metavm.entity;

import org.metavm.flow.ClosureContext;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public interface NativeObject extends ClassInstance {

    @Override
    default void logFields() {
    }

    @Override
    default void forEachField(BiConsumer<Field, Value> action) {
    }

    @Override
    default void defaultWrite(InstanceOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void defaultRead(InstanceInput input) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setField(org.metavm.object.type.Field field, org.metavm.object.instance.core.Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setFieldForce(org.metavm.object.type.Field field, org.metavm.object.instance.core.Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isFieldInitialized(org.metavm.object.type.Field field) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    default org.metavm.object.type.Field findUninitializedField(Klass type) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void initField(org.metavm.object.type.Field field, org.metavm.object.instance.core.Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default org.metavm.object.instance.core.Value getField(org.metavm.object.type.Field field) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void ensureAllFieldsInitialized() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    default ClosureContext getClosureContext() {
        return null;
    }

    @Override
    default void addChild(ClassInstance child) {
        throw new UnsupportedOperationException();
    }

}
