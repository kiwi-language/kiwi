package org.metavm.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
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

@NativeEntity(92)
@Entity
public class Qux extends org.metavm.entity.Entity {

    public static final IndexDef<Qux> IDX_AMOUNT = IndexDef.create(Qux.class,
            1, qux -> List.of(Instances.longInstance(qux.amount)));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private long amount;

    public Qux(long amount) {
        this.amount = amount;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitLong();
    }

    public long getAmount() {
        return amount;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("amount", this.getAmount());
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
        return EntityRegistry.TAG_Qux;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.amount = input.readLong();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeLong(amount);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
