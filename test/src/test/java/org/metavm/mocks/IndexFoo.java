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

@NativeEntity(93)
@Entity
public class IndexFoo extends org.metavm.entity.Entity {

    public static final IndexDef<IndexFoo> IDX_STATE = IndexDef.create(IndexFoo.class,
            1, indexFoo -> List.of(Instances.intInstance(indexFoo.state.code())));
    public static final IndexDef<IndexFoo> IDX_CODE = IndexDef.create(IndexFoo.class,
            1, indexFoo -> List.of(Instances.intInstance(indexFoo.code)));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private FooState state = FooState.STATE1;
    private int code;

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitByte();
        visitor.visitInt();
    }

    public FooState getState() {
        return state;
    }

    public void setState(FooState state) {
        this.state = state;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
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
        map.put("state", this.getState().name());
        map.put("code", this.getCode());
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
        return EntityRegistry.TAG_IndexFoo;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.state = FooState.fromCode(input.read());
        this.code = input.readInt();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.write(state.code());
        output.writeInt(code);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
