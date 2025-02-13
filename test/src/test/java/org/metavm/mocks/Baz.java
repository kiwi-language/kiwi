package org.metavm.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(97)
@Entity
public class Baz extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private List<Reference> bars = new ArrayList<>();

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitList(visitor::visitValue);
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    public Baz(Id id) {
        super(id);
    }

    public Baz(Id id, List<Bar> bars) {
        super(id);
        setBars(bars);
    }

    public List<Bar> getBars() {
        return Utils.map(bars, r -> (Bar) r.get());
    }

    public void setBars(List<Bar> bars) {
        this.bars = Utils.map(bars, Instance::getReference);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var bars_ : bars) action.accept(bars_);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("bars", this.getBars().stream().map(org.metavm.entity.Entity::getStringId).toList());
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
        return EntityRegistry.TAG_Baz;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.bars = input.readList(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeList(bars, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
