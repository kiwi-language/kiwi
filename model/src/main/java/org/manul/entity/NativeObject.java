package org.manul.entity;

import org.manul.flow.ClosureContext;
import org.manul.object.instance.core.*;
import org.manul.object.type.Field;
import org.manul.object.type.Klass;
import org.manul.util.InstanceInput;
import org.manul.util.InstanceOutput;

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
    default void setField(org.manul.object.type.Field field, org.manul.object.instance.core.Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void setFieldForce(org.manul.object.type.Field field, org.manul.object.instance.core.Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isFieldInitialized(org.manul.object.type.Field field) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    default org.manul.object.type.Field findUninitializedField(Klass type) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void initField(org.manul.object.type.Field field, org.manul.object.instance.core.Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    default org.manul.object.instance.core.Value getField(org.manul.object.type.Field field) {
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
