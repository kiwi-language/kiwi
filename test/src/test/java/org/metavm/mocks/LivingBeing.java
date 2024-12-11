package org.metavm.mocks;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.entity.ReadWriteArray;

import java.util.List;

@Entity
public class LivingBeing extends org.metavm.entity.Entity {

    public static final IndexDef<LivingBeing> IDX_AGE = IndexDef.create(
        LivingBeing.class, "age"
    );

    private long age;

    private Object extraInfo;

    @ChildEntity
    private final ReadWriteArray<LivingBeing> offsprings = addChild(new ReadWriteArray<>(LivingBeing.class), "offsprings");

    @ChildEntity
    private final ReadWriteArray<LivingBeing> ancestors = addChild(new ReadWriteArray<>(LivingBeing.class), "ancestors");

    public LivingBeing(long age) {
        this.age = age;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public Object getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Object extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void addOffspring(LivingBeing offspring) {
        offsprings.add(offspring);
    }

    public void removeOffspring(LivingBeing offspring) {
        offsprings.remove(offspring);
    }

    public List<LivingBeing> getOffsprings() {
        return offsprings.toList();
    }

    public void clearOffsprings() {
        offsprings.clear();
    }

    public List<LivingBeing> getAncestors() {
        return ancestors.toList();
    }

    public void addAncestor(LivingBeing ancestor) {
        ancestors.add(ancestor);
    }

    public void removeAncestor(LivingBeing ancestor) {
        ancestors.remove(ancestor);
    }

}
