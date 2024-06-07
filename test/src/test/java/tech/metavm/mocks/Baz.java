package tech.metavm.mocks;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ReadWriteArray;

import java.util.List;

@EntityType
public class Baz extends Entity {

    @ChildEntity
    private ReadWriteArray<Bar> bars = addChild(new ReadWriteArray<>(Bar.class), "bars");

    public Baz() {
    }

    public Baz(List<Bar> bars) {
        setBars(bars);
    }

    public ReadWriteArray<Bar> getBars() {
        return bars;
    }

    public void setBars(List<Bar> bars) {
        this.bars = addChild(new ReadWriteArray<>(Bar.class, bars), "bars");
    }
}
