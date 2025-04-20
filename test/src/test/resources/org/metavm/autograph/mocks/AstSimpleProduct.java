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
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(100)
@Entity(compiled = true)
public class AstSimpleProduct extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    @EntityField(asTitle = true)
    public String title;

    public int orderCount;

    public long price;

    public int inventory;

    public AstProductState state;

    public AstSimpleProduct(Id id) {
        super(id);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitInt();
        visitor.visitLong();
        visitor.visitInt();
        visitor.visitByte();
    }

    public long calcOrderPrice(int amount, AstSimpleCoupon[] coupons) {
        long discount = 0;
        for (int i = 0; i < coupons.length; i++) {
            discount += coupons[i].discount;
        }
        return price * amount - discount;
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
        return EntityRegistry.TAG_AstSimpleProduct;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.title = input.readUTF();
        this.orderCount = input.readInt();
        this.price = input.readLong();
        this.inventory = input.readInt();
        this.state = AstProductState.fromCode(input.read());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(title);
        output.writeInt(orderCount);
        output.writeLong(price);
        output.writeInt(inventory);
        output.write(state.code());
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
