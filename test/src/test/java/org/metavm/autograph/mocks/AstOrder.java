package org.metavm.autograph.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(89)
@Entity(compiled = true)
public class AstOrder extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    @EntityField(asTitle = true)
    public String code;

    public long price;

    public Reference product;

    public int amount;

    public List<Reference> coupons;

    public int state;

    public AstOrder(Id id, String code, long price, AstProduct product, int amount, List<AstCoupon> coupons) {
        super(id);
        this.code = code;
        this.price = price;
        this.product = product.getReference();
        this.amount = amount;
        this.coupons = Utils.map(coupons, c -> ((Instance)c).getReference());
        this.state = 0;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitLong();
        visitor.visitValue();
        visitor.visitInt();
        visitor.visitList(visitor::visitValue);
        visitor.visitInt();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(product);
        for (var coupons_ : coupons) action.accept(coupons_);
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
        return EntityRegistry.TAG_AstOrder;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.code = input.readUTF();
        this.price = input.readLong();
        this.product = (Reference) input.readValue();
        this.amount = input.readInt();
        this.coupons = input.readList(() -> (Reference) input.readValue());
        this.state = input.readInt();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(code);
        output.writeLong(price);
        output.writeValue(product);
        output.writeInt(amount);
        output.writeList(coupons, output::writeValue);
        output.writeInt(state);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
