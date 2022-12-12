package tech.metavm.mocks;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("人")
public class Human extends Animal {

    @EntityField("职业")
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
