package org.metavm.entity.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(91)
@Entity
public class EntityFoo extends org.metavm.entity.Entity {

    public static final IndexDef<EntityFoo> idxName
            = IndexDef.create(EntityFoo.class, 1, f -> List.of(Instances.stringInstance(f.name)));

    public static final IndexDef<EntityFoo> idxBar
            = IndexDef.create(EntityFoo.class, 1 , f -> List.of(f.bar));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public String name;
    private Reference bar;
    @Nullable
    private ValueBaz baz;

    public EntityFoo(Id id, String name, EntityBar bar) {
        super(id);
        this.name = name;
        this.bar = bar.getReference();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitValue();
        visitor.visitNullable(() -> ValueBaz.visit(visitor));
    }

    public EntityBar getBar() {
        return (EntityBar) bar.get();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(bar);
        if (baz != null) baz.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("bar", this.getBar().getStringId());
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
        return EntityRegistry.TAG_EntityFoo;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.name = input.readUTF();
        this.bar = (Reference) input.readValue();
        this.baz = input.readNullable(() -> ValueBaz.read(input));
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(name);
        output.writeValue(bar);
        output.writeNullable(baz, arg0 -> arg0.write(output));
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
