package tech.metavm.mocks;

import tech.metavm.entity.*;

import java.util.List;

@EntityType("生物")
public class LivingBeing extends Entity {

    public static final IndexDef<LivingBeing> IDX_AGE = IndexDef.normalKey(
        LivingBeing.class, "age"
    );

    @EntityField("年龄")
    private long age;

    @EntityField("额外信息")
    private Object extraInfo;

    @ChildEntity("后代")
    private final ReadWriteArray<LivingBeing> offsprings = addChild(new ReadWriteArray<>(LivingBeing.class), "offsprings");

    @ChildEntity("祖先")
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
        return offsprings;
    }

    public void clearOffsprings() {
        offsprings.clear();
    }

    public List<LivingBeing> getAncestors() {
        return ancestors;
    }

    public void addAncestor(LivingBeing ancestor) {
        ancestors.add(ancestor);
    }

    public void removeAncestor(LivingBeing ancestor) {
        ancestors.remove(ancestor);
    }

}
