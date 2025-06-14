package org.metavm.object.instance.core;

import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.CharSequenceNative;
import org.metavm.flow.ClosureContext;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.metavm.util.Instances.toJavaString;

public class StringInstance implements CharSequenceNative, ClassInstance {

    private final StringReference ref;
    private String value;

    public StringInstance(String value) {
        ref = new StringReference(this, value);
        this.value = value;
    }

    // <editor-fold defaultstate="collapsed" desc="nativeMethods">

    public Value hashCode(CallContext callContext) {
        return Instances.intInstance(value.hashCode());
    }

    public Value equals(Value that, CallContext callContext) {
        var thatValue = toJavaString(that);
        return Instances.intInstance(value.equals(thatValue));
    }

    public Value toString(CallContext callContext) {
        return ref;
    }

    public Value isEmpty(CallContext callContext) {
        return Instances.intInstance(value.isEmpty());
    }

    public Value length(CallContext callContext) {
        return Instances.intInstance(value.length());
    }

    public Value compareTo(Value that, CallContext callContext) {
        return Instances.intInstance(value.compareTo(toJavaString(that)));
    }

    public Value startsWith(Value s, CallContext callContext) {
        return Instances.intInstance(value.startsWith(toJavaString(s)));
    }

    public Value endsWith(Value s, CallContext callContext) {
        return Instances.intInstance(value.endsWith(toJavaString(s)));
    }

    public Value contains(Value s, CallContext callContext) {
        return Instances.intInstance(value.contains(toJavaString(s)));
    }

    public Value concat(Value s, CallContext callContext) {
        return Instances.stringInstance(value.concat(toJavaString(s)));
    }

    public Value replace__CharSequence_CharSequence(Value target, Value replacement, CallContext callContext) {
        return Instances.stringInstance(
                value.replace(toJavaString(target), toJavaString(replacement))
        );
    }

    public Value replace__char_char(Value oldChar, Value newChar, CallContext callContext) {
        var c1 = (IntValue) oldChar;
        var c2 = (IntValue) newChar;
        return Instances.stringInstance(value.replace((char) c1.value, (char) c2.value));
    }

    public Value replaceFirst(Value regex, Value replacement, CallContext callContext) {
        return Instances.stringInstance(
                value.replaceFirst(toJavaString(regex), toJavaString(replacement))
        );
    }

    public Value replaceAll(Value regex, Value replacement, CallContext callContext) {
        return Instances.stringInstance(
                value.replaceAll(toJavaString(regex), toJavaString(replacement))
        );
    }

    public Value substring(Value beginIndex, CallContext callContext) {
        var i = (IntValue) beginIndex;
        return Instances.stringInstance(value.substring(i.value));
    }

    public Value substring(Value beginIndex, Value endIndex, CallContext callContext) {
        var i1 = (IntValue) beginIndex;
        var i2 = (IntValue) endIndex;
        return Instances.stringInstance(value.substring(i1.value, i2.value));
    }

    public Value subSequence(Value beginIndex, Value endIndex, CallContext callContext) {
        return substring(beginIndex, endIndex, callContext);
    }

    public Value charAt(Value index, CallContext callContext) {
        var i = (IntValue) index;
        return Instances.intInstance(value.charAt(i.value));
    }

    public Value writeObject(Value s, CallContext callContext) {
        var output = ((MvObjectOutputStream) s.resolveObject()).getOut();
        output.writeUTF(value);
        return Instances.nullInstance();
    }

    public Value readObject(Value s, CallContext callContext) {
        var input = ((MvObjectInputStream) s.resolveObject()).getInput();
        value = input.readUTF();
        return Instances.nullInstance();
    }

    public static Value format(Klass klass, Value format, Value args, CallContext callContext) {
        var f = toJavaString(format);
        var a = (Object[]) Instances.toJavaValue(args, Object[].class);
        return Instances.stringInstance(String.format(f, a));
    }

    public static boolean startsWith(Value s1, Value s2) {
        return toJavaString(s1).startsWith(toJavaString(s2));
    }

    public static boolean endsWith(Value s1, Value s2) {
        return toJavaString(s1).endsWith(toJavaString(s2));
    }

    public static boolean equals(Value s1, Value s2) {
        return toJavaString(s1).equals(toJavaString(s2));
    }

    public static int compare(Value s1, Value s2) {
        return toJavaString(s1).compareTo(toJavaString(s2));
    }

    public static boolean contains(Value s1, Value s2) {
        return toJavaString(s1).contains(toJavaString(s2));
    }

    public static Value concat(Value s1, Value s2) {
        return Instances.stringInstance(toJavaString(s1).concat(toJavaString(s2)));
    }

    public static Value replace(Value s, Value target, Value replacement) {
        var js = toJavaString(s);
        var jTarget = toJavaString(target);
        var jReplacement = toJavaString(replacement);
        return Instances.stringInstance(js.replace(jTarget, jReplacement));
    }

    public static Value replaceFirst(Value s, Value regex, Value replacement) {
        var js = toJavaString(s);
        var jRegex = toJavaString(regex);
        var jReplacement = toJavaString(replacement);
        return Instances.stringInstance(js.replaceFirst(jRegex, jReplacement));
    }

    public static Value replaceAll(Value s, Value regex, Value replacement) {
        var js = toJavaString(s);
        var jRegex = toJavaString(regex);
        var jReplacement = toJavaString(replacement);
        return Instances.stringInstance(js.replaceAll(jRegex, jReplacement));
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="classInstance">


    @Override
    public Id tryGetId() {
        return null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Long tryGetTreeId() {
        return null;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public Reference getReference() {
        return ref;
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {

    }

    @Override
    public void forEachValue(Consumer<? super Instance> action) {

    }

    @Override
    public Instance getRoot() {
        return this;
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitClassInstance(this);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {

    }

    @Override
    public void write(MvOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InstanceState state() {
//        return state;
        return null;
    }

    @Nullable
    @Override
    public Instance getParent() {
        return null;
    }

    @Override
    public String getText() {
        return "String " + value;
    }

    @Override
    public void logFields() {

    }

    @Override
    public void forEachField(BiConsumer<Field, Value> action) {

    }

    @Override
    public ClassType getInstanceType() {
        return StdKlass.string.type();
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public void defaultWrite(InstanceOutput output) {

    }

    @Override
    public void defaultRead(InstanceInput input) {

    }

    @Override
    public void setField(Field field, Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFieldForce(Field field, Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFieldInitialized(Field field) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Field findUninitializedField(Klass type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initField(Field field, Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value getField(Field field) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void tryClearUnknownField(long klassTag, int tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value getUnknownField(long klassTag, int tag) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Value tryGetUnknown(long klassId, int tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void ensureAllFieldsInitialized() {

    }

    @Override
    public Klass getInstanceKlass() {
        return StdKlass.string.get();
    }

    @Override
    public void setUnknown(long classTag, int fieldTag, Value value) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public ClosureContext getClosureContext() {
        return null;
    }

    @Override
    public void addChild(ClassInstance child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Value> buildSource() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(MvOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isValue() {
        return true;
    }

    @Override
    public Instance copy(Function<ClassType, Id> idSupplier) {
        return this;
    }

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public int getRefcount() {
        return 0;
    }

    @Override
    public void incRefcount(int amount) {
    }

    // </editor-fold>

}
