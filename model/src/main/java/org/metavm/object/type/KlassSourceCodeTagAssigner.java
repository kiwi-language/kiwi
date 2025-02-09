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

@NativeEntity(8)
@Entity
public class KlassSourceCodeTagAssigner extends org.metavm.entity.Entity {

    public static final IndexDef<KlassSourceCodeTagAssigner> IDX_ALL_FLAGS = IndexDef.create(KlassSourceCodeTagAssigner.class,
            1, e -> List.of(Instances.booleanInstance(e.allFlags)));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static KlassSourceCodeTagAssigner getInstance(EntityRepository context) {
        return Objects.requireNonNull(
                context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance()),
                "ClassTagAssigner instance not found"
        );
    }

    @SuppressWarnings("unused")
    private boolean allFlags = true;

    private int nextTag = 1000000;

    public KlassSourceCodeTagAssigner(Id id) {
        super(id);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitBoolean();
        visitor.visitInt();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    public static void initialize(IInstanceContext context) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance());
        if(existing != null)
            throw new IllegalStateException("ClassTagAssigner already exists");
        context.bind(new KlassSourceCodeTagAssigner(context.allocateRootId()));
    }

    public int next() {
        return nextTag++;
    }

    public int getNextTag() {
        return nextTag;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("nextTag", this.getNextTag());
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
        return EntityRegistry.TAG_KlassSourceCodeTagAssigner;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.allFlags = input.readBoolean();
        this.nextTag = input.readInt();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeBoolean(allFlags);
        output.writeInt(nextTag);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
