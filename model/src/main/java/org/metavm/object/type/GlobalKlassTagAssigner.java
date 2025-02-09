package org.metavm.object.type;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@NativeEntity(40)
@Entity
public class GlobalKlassTagAssigner extends org.metavm.entity.Entity {

    public static final IndexDef<GlobalKlassTagAssigner> IDX_ALL_FLAGS = IndexDef.create(GlobalKlassTagAssigner.class,
            1, e -> List.of(Instances.booleanInstance(e.allFlags)));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static GlobalKlassTagAssigner initialize(IInstanceContext context) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance());
        if (existing != null)
            throw new IllegalStateException("GlobalKlassTagAssigner already exists");
        return context.bind(new GlobalKlassTagAssigner(context.allocateRootId()));
    }

    public static GlobalKlassTagAssigner getInstance(IInstanceContext context) {
        return Objects.requireNonNull(
                context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance()),
                "GlobalKlassTagAssigner instance not found"
        );
    }

    @SuppressWarnings("unused")
    private boolean allFlags = true;
    private long next = 1000000;

    public GlobalKlassTagAssigner(Id id) {
        super(id);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitBoolean();
        visitor.visitLong();
    }

    public long[] allocate(long size) {
        return new long[]{next, next += size};
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
        return EntityRegistry.TAG_GlobalKlassTagAssigner;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.allFlags = input.readBoolean();
        this.next = input.readLong();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeBoolean(allFlags);
        output.writeLong(next);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
