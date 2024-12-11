package org.metavm.mocks;

import org.metavm.api.Entity;

@Entity
public class Human extends Animal {

    private String occupation;

    public Human(long age, long intelligence, String occupation) {
        super(age, intelligence);
        this.occupation = occupation;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
}
