package org.metavm.autograph.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.NativeApi;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.HashedValue;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(81)
@Entity(compiled = true)
public class AstBranchFoo extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private AstProductState state = AstProductState.NORMAL;
    private int inventory = 0;

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitByte();
        visitor.visitInt();
    }

    public void dec(int amount) {
        if(state != AstProductState.NORMAL || this.inventory < amount)
            throw new RuntimeException("Product is out of inventory of off shelf");
        this.inventory -= amount;
    }

    public void putOnShelf() {
        state = AstProductState.NORMAL;
    }

    public void takeOffShelf() {
        state = AstProductState.OFF_THE_SHELF;
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
        return EntityRegistry.TAG_AstBranchFoo;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.state = AstProductState.fromCode(input.read());
        this.inventory = input.readInt();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.write(state.code());
        output.writeInt(inventory);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
