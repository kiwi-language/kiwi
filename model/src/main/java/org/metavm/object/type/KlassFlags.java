package org.metavm.object.type;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(59)
@Entity(since = 1)
public class KlassFlags extends org.metavm.entity.Entity {
    @SuppressWarnings("unused")
    private static Klass __klass__;
    private boolean flag1;
    private Klass klass;

    public KlassFlags(Klass klass) {
        this.klass = klass;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitBoolean();
    }

    public boolean isFlag1() {
        return flag1;
    }

    public void setFlag1(boolean flag1) {
        this.flag1 = flag1;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return klass;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("flag1", this.isFlag1());
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
        return EntityRegistry.TAG_KlassFlags;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.klass = (Klass) parent;
        this.flag1 = input.readBoolean();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeBoolean(flag1);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
