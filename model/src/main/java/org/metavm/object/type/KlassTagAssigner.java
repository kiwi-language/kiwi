package org.metavm.object.type;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.EntityRepository;
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

@NativeEntity(64)
@Entity
public class KlassTagAssigner extends org.metavm.entity.Entity {

    public static final IndexDef<KlassTagAssigner> IDX_ALL_FLAGS = IndexDef.create(KlassTagAssigner.class, 1,
            e -> List.of(Instances.booleanInstance(e.allFlags)));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static KlassTagAssigner getInstance(EntityRepository context) {
        return Objects.requireNonNull(
                context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance()),
                "ClassTagAssigner instance not found"
        );
    }

    @SuppressWarnings("unused")
    private boolean allFlags = true;

    private long start;
    private long next;
    private long max;

    public KlassTagAssigner(Id id, long start, long max) {
        super(id);
        this.start = next = start;
        this.max = max;
    }

    public static void initialize(IInstanceContext context, GlobalKlassTagAssigner globalKlassTagAssigner) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance());
        if(existing != null)
            throw new IllegalStateException("ClassTagAssigner already exists");
        var range = globalKlassTagAssigner.allocate(1000000);
        context.bind(new KlassTagAssigner(context.allocateRootId(), range[0], range[1]));
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitBoolean();
        visitor.visitLong();
        visitor.visitLong();
        visitor.visitLong();
    }

    public long next() {
        if(next >= max)
            throw new IllegalStateException("No more class tags available");
        return next++;
    }

    public void assign(long start, long max) {
        this.start = next = start;
        this.max = max;
    }

    public long getStart() {
        return start;
    }

    public long getNextTag() {
        return next;
    }

    public long getMax() {
        return max;
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
        map.put("start", this.getStart());
        map.put("nextTag", this.getNextTag());
        map.put("max", this.getMax());
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
        return EntityRegistry.TAG_KlassTagAssigner;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.allFlags = input.readBoolean();
        this.start = input.readLong();
        this.next = input.readLong();
        this.max = input.readLong();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeBoolean(allFlags);
        output.writeLong(start);
        output.writeLong(next);
        output.writeLong(max);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
