package org.metavm.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(82)
@Entity(since = 1)
public class UpgradeValue extends org.metavm.entity.Entity {

    public static final IndexDef<UpgradeValue> IDX_FOO = IndexDef.createUnique(UpgradeValue.class,
            1, upgradeValue -> List.of(upgradeValue.foo));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private Reference foo;
    private Value value;

    public UpgradeValue(UpgradeFoo foo) {
        this.foo = foo.getReference();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitValue();
        visitor.visitValue();
    }

    public UpgradeFoo getFoo() {
        return (UpgradeFoo) foo.get();
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(foo);
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("foo", this.getFoo().getStringId());
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
        return EntityRegistry.TAG_UpgradeValue;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.foo = (Reference) input.readValue();
        this.value = input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeValue(foo);
        output.writeValue(value);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }
}
