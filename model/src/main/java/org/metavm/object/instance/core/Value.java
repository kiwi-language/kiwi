package org.metavm.object.instance.core;

import org.metavm.wire.Wire;
import org.metavm.flow.ClosureContext;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;

@Wire(adapter = ValueAdapter.class)
public interface Value {

    default Type getValueType() {
        throw new UnsupportedOperationException();
    }

    default boolean isValue() {
        return false;
    }

    default boolean isNull() {
        return false;
    }

    default boolean isNotNull() {
        return !isNull();
    }

    default boolean isPassword() {
        return getValueType().isPassword();
    }

    default Value toStringInstance() {
        return Instances.stringInstance(getTitle());
    }

    default boolean isArray() {
        return false;
    }

    default boolean isObject() {
        return false;
    }

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
