package tech.metavm.mocks;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("动物")
public class Animal extends LivingBeing {

    @EntityField("智力")
    private long intelligence;

    public Animal(long age, long intelligence) {
        super(age);
        this.intelligence = intelligence;
    }

    public long getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(long intelligence) {
        this.intelligence = intelligence;
    }
}
