package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.ValueObject;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.LocalKey;
import org.metavm.entity.Struct;
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

//@NativeEntity(6)
//@Entity
public class StaticFieldTableEntry implements LocalKey, Struct {
    @SuppressWarnings("unused")
    private static Klass __klass__;
    private StaticFieldTable table;
    private Reference fieldReference;
    private Value value;

    public StaticFieldTableEntry(StaticFieldTable table) {
        this.table = table;
    }

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

    @Generated
    public static StaticFieldTableEntry read(MvInput input, Object parent) {
        var r = new StaticFieldTableEntry((StaticFieldTable) parent);
        r.fieldReference = (Reference) input.readValue();
        r.value = input.readValue();
        return r;
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
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

    public void forEachReference(Consumer<Reference> action) {
        action.accept(fieldReference);
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    public void buildJson(Map<String, Object> map) {
        map.put("field", this.getField().getStringId());
        map.put("value", this.getValue().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.writeValue(fieldReference);
        output.writeValue(value);
    }

    public Map<String, Object> toJson() {
        var map = new java.util.HashMap<String, Object>();
        buildJson(map);
        return map;
    }

    //    public void buildJson(Map<String, Object> map) {
//        map.put("field", this.getField().getStringId());
//        map.put("value", this.getValue().toJson());
//        var parentEntity = this.getParentEntity();
//        if (parentEntity != null) map.put("parentEntity", parentEntity.getStringId());
//        map.put("title", this.getTitle());
//        map.put("instanceKlass", this.getInstanceKlass().getStringId());
//        map.put("instanceType", this.getInstanceType().toJson());
//        map.put("entityTag", this.getEntityTag());
//    }

    //    public Map<String, Object> toJson() {
//        var map = new java.util.HashMap<String, Object>();
//        buildJson(map);
//        return map;
//    }
}
