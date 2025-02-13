package org.metavm.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(83)
@Entity
public class LivingBeing extends org.metavm.entity.Entity {

    public static final IndexDef<LivingBeing> IDX_AGE = IndexDef.create(
        LivingBeing.class, 1, livingBeing -> List.of(Instances.longInstance(livingBeing.age))
    );
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private long age;

    private Value extraInfo;

    private List<Reference> offsprings = new ArrayList<>();

    private List<Reference> ancestors = new ArrayList<>();

    public LivingBeing(Id id, long age) {
        super(id);
        this.age = age;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitLong();
        visitor.visitValue();
        visitor.visitList(visitor::visitValue);
        visitor.visitList(visitor::visitValue);
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public Value getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Value extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void addOffspring(LivingBeing offspring) {
        offsprings.add(offspring.getReference());
    }

    public void removeOffspring(LivingBeing offspring) {
        offsprings.remove(offspring);
    }

    public List<LivingBeing> getOffsprings() {
        return Utils.map(offsprings, r -> (LivingBeing) r.get());
    }

    public void clearOffsprings() {
        offsprings.clear();
    }

    public List<LivingBeing> getAncestors() {
        return Utils.map(ancestors, r -> (LivingBeing) r.get());
    }

    public void addAncestor(LivingBeing ancestor) {
        ancestors.add(ancestor.getReference());
    }

    public void removeAncestor(LivingBeing ancestor) {
        ancestors.remove(ancestor.getReference());
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (extraInfo instanceof Reference r) action.accept(r);
        else if (extraInfo instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
        for (var offsprings_ : offsprings) action.accept(offsprings_);
        for (var ancestors_ : ancestors) action.accept(ancestors_);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("age", this.getAge());
        map.put("extraInfo", this.getExtraInfo().toJson());
        map.put("offsprings", this.getOffsprings().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("ancestors", this.getAncestors().stream().map(org.metavm.entity.Entity::getStringId).toList());
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
        return EntityRegistry.TAG_LivingBeing;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.age = input.readLong();
        this.extraInfo = input.readValue();
        this.offsprings = input.readList(() -> (Reference) input.readValue());
        this.ancestors = input.readList(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeLong(age);
        output.writeValue(extraInfo);
        output.writeList(offsprings, output::writeValue);
        output.writeList(ancestors, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
    }
}
