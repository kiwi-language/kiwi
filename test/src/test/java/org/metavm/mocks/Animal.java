package org.metavm.mocks;

import org.metavm.api.Entity;

@Entity
public class Animal extends LivingBeing {

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
