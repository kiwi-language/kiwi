package org.metavm.object.version;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.ChildEntity;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@NativeEntity(76)
@Entity
public class Version extends org.metavm.entity.Entity {

    public static final IndexDef<Version> IDX_VERSION = IndexDef.createUnique(Version.class,
            1, v -> List.of(Instances.longInstance(v.version)));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private long version;

    private List<String> changedTypeIds = new ArrayList<>();

    private List<String> removedTypeIds = new ArrayList<>();

    private List<String> changedFunctionIds = new ArrayList<>();

    private List<String> removedFunctionIds = new ArrayList<>();

    public Version(long version,
                   Set<String> changedTypeIds,
                   Set<String> removedTypeIds,
                   Set<String> changedFunctionIds,
                   Set<String> removedFunctionIds
    ) {
        this.version = version;
        this.changedTypeIds.addAll(changedTypeIds);
        this.removedTypeIds.addAll(removedTypeIds);
        this.changedFunctionIds.addAll(changedFunctionIds);
        this.removedFunctionIds.addAll(removedFunctionIds);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitLong();
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
        visitor.visitList(visitor::visitUTF);
    }

    public long getVersion() {
        return version;
    }

    public List<String> getChangedTypeIds() {
        return Collections.unmodifiableList(changedTypeIds);
    }

    public List<String> getRemovedTypeIds() {
        return Collections.unmodifiableList(removedTypeIds);
    }

    public List<String> getChangedFunctionIds() {
        return Collections.unmodifiableList(changedFunctionIds);
    }

    public List<String> getRemovedFunctionIds() {
        return Collections.unmodifiableList(removedFunctionIds);
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
        map.put("changedTypeIds", this.getChangedTypeIds());
        map.put("removedTypeIds", this.getRemovedTypeIds());
        map.put("changedFunctionIds", this.getChangedFunctionIds());
        map.put("removedFunctionIds", this.getRemovedFunctionIds());
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
        return EntityRegistry.TAG_Version;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.version = input.readLong();
        this.changedTypeIds = input.readList(input::readUTF);
        this.removedTypeIds = input.readList(input::readUTF);
        this.changedFunctionIds = input.readList(input::readUTF);
        this.removedFunctionIds = input.readList(input::readUTF);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeLong(version);
        output.writeList(changedTypeIds, output::writeUTF);
        output.writeList(removedTypeIds, output::writeUTF);
        output.writeList(changedFunctionIds, output::writeUTF);
        output.writeList(removedFunctionIds, output::writeUTF);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
