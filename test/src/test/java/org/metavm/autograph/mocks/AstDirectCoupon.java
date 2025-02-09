package org.metavm.autograph.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.api.NativeApi;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Id;
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

@NativeEntity(88)
@Entity(compiled = true)
public class AstDirectCoupon extends org.metavm.entity.Entity implements AstCoupon {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    public long discount;

    public AstCouponState state;

    public Reference product;

    public AstDirectCoupon(Id id) {
        super(id);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitLong();
        visitor.visitByte();
        visitor.visitValue();
    }

    public long use(int amount) {
        if(state != AstCouponState.UNUSED) {
            throw new RuntimeException("The coupon is already used");
        }
        state = AstCouponState.USED;
        return discount;
    }

    @Override
    public long calc(int amount) {
        return discount;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(product);
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
        return EntityRegistry.TAG_AstDirectCoupon;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.discount = input.readLong();
        this.state = AstCouponState.fromCode(input.read());
        this.product = (Reference) input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeLong(discount);
        output.write(state.code());
        output.writeValue(product);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
