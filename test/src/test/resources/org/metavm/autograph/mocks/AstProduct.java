package org.metavm.autograph.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(90)
@Entity(compiled = true)
public class AstProduct extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    @EntityField(asTitle = true)
    public String title;

    public long orderCount;

    public long price;

    public long inventory;

    public AstProductState state;

    public AstProduct(Id id) {
        super(id);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitLong();
        visitor.visitLong();
        visitor.visitLong();
        visitor.visitByte();
    }

    public void dec(int amount) {
        if (state != AstProductState.NORMAL || inventory < amount) {
            throw new RuntimeException("Product out of inventory of off shelf");
        }
        inventory -= amount;
    }

    public <CouponType extends AstCoupon> long calcDiscount(List<CouponType> coupons) {
        long discount = 0L;
        for (CouponType coupon : coupons) {
            discount += coupon.calc(1);
        }
        return discount;
    }

    public long calcDirectDiscount(AstDirectCoupon[] directCoupons) {
        List<AstCoupon> list = new ArrayList<>();
        //noinspection ManualArrayToCollectionCopy
        for (AstDirectCoupon directCoupon : directCoupons) {
            //noinspection UseBulkOperation
            list.add(directCoupon);
        }
        return calcDiscount(list);
    }

    public AstOrder buy(int amount, AstCoupon[] coupons, IInstanceContext context) {
        dec(amount);
        List<AstCoupon> selectedCoupons = new ArrayList<>();
        long orderPrice = amount * price;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < coupons.length; i++) {
            orderPrice -= coupons[i].use(amount);
            selectedCoupons.add(coupons[i]);
        }
        return new AstOrder(
                context.allocateRootId(),
                title + ++orderCount,
                orderPrice,
                this,
                amount,
                selectedCoupons
        );
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
        return EntityRegistry.TAG_AstProduct;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.title = input.readUTF();
        this.orderCount = input.readLong();
        this.price = input.readLong();
        this.inventory = input.readLong();
        this.state = AstProductState.fromCode(input.read());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(title);
        output.writeLong(orderCount);
        output.writeLong(price);
        output.writeLong(inventory);
        output.write(state.code());
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
