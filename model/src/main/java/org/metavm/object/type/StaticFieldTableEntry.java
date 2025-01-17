package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.LocalKey;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(6)
@Entity
public class StaticFieldTableEntry extends org.metavm.entity.Entity implements LocalKey {
    @SuppressWarnings("unused")
    private static Klass __klass__;
    private StaticFieldTable table;
    private Reference fieldReference;
    private Value value;

    public StaticFieldTableEntry(StaticFieldTable table, Field field, Value value) {
        this.table = table;
        this.fieldReference = field.getReference();
        this.value = value;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitValue();
        visitor.visitValue();
    }

    public Field getField() {
        return (Field) fieldReference.get();
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return getField().getName();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return table;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(fieldReference);
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("field", this.getField().getStringId());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_StaticFieldTableEntry;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.table = (StaticFieldTable) parent;
        this.fieldReference = (Reference) input.readValue();
        this.value = input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeValue(fieldReference);
        output.writeValue(value);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }
}
