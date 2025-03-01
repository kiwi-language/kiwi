package org.metavm.object.instance.core;

import org.metavm.entity.NoProxy;
import org.metavm.flow.ClosureContext;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;

public interface Value {

    @NoProxy
    Type getValueType();

    @NoProxy
    default boolean isValue() {
        return false;
    }

    @NoProxy
    default boolean isNull() {
        return false;
    }

    @NoProxy
    default boolean isNotNull() {
        return !isNull();
    }

    @NoProxy
    default boolean isPassword() {
        return getValueType().isPassword();
    }

    default Value toStringInstance() {
        return Instances.stringInstance(getTitle());
    }

    @NoProxy
    default boolean isArray() {
        return false;
    }

    default boolean isObject() {
        return false;
    }

    @NoProxy
    default boolean isPrimitive() {
        return false;
    }

    default String toStringValue() {
        throw new UnsupportedOperationException();
    }

    default boolean isEphemeral() {
        return false;
    }

    default boolean shouldSkipWrite() {
        return false;
    }

    String getTitle();


    void writeInstance(MvOutput output) ;

    void write(MvOutput output);

    Object toSearchConditionValue();

    @NoProxy
    <R> R accept(ValueVisitor<R> visitor);

    default String getText() {
        var treeWriter = new TreeWriter();
        writeTree(treeWriter);
        return treeWriter.toString();
    }

    void writeTree(TreeWriter treeWriter);

    Object toJson();

    default ClassInstance resolveObject() {
        return (ClassInstance) resolveDurable();
    }

    default ArrayInstance resolveArray() {
        return (ArrayInstance) resolveDurable();
    }

    default Instance resolveDurable(){
        return ((Reference) this).get();
    }

    default MvInstance resolveMv() {
        return (MvInstance) ((Reference) this).get();
    }

    default MvClassInstance resolveMvObject() {
        return (MvClassInstance) ((Reference) this).get();
    }

    default Value toStackValue() {
        return this;
    }

    default ClosureContext getClosureContext() {
        return null;
    }

    default String stringValue() {
        throw new UnsupportedOperationException();
    }

}
