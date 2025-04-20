package org.metavm.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(98)
@Entity
public class UpgradeFoo extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private String name;
    @EntityField(since = 1)
    private Value bar;
    @EntityField
    private Value value;
    @EntityField(since = 1)
    private List<Reference> array = new ArrayList<>();

    public UpgradeFoo(Id id, String name, Value bar) {
        super(id);
        this.name = name;
        this.bar = bar;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitValue();
        visitor.visitValue();
        visitor.visitList(visitor::visitValue);
    }

    public String getName() {
        return name;
    }

    public Value getBar() {
        return bar;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public List<UpgradeFoo> getArray() {
        return Utils.map(array, e -> (UpgradeFoo) e.get());
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public String toString() {
        return "name: " + name + ", bar: " + bar;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (bar instanceof Reference r) action.accept(r);
        else if (bar instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
        for (var array_ : array) action.accept(array_);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("name", this.getName());
        map.put("bar", this.getBar().toJson());
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
        return EntityRegistry.TAG_UpgradeFoo;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.name = input.readUTF();
        this.bar = input.readValue();
        this.value = input.readValue();
        this.array = input.readList(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(name);
        output.writeValue(bar);
        output.writeValue(value);
        output.writeList(array, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }
}