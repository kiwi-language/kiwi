package tech.metavm.mocks;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.util.ReadWriteArray;

import java.util.List;

@EntityType("巴子")
public class Baz extends Entity {

    @ChildEntity("巴巴巴巴")
    private ReadWriteArray<Bar> bars = new ReadWriteArray<>(Bar.class);

    public Baz() {
    }

    public Baz(List<Bar> bars) {
        setBars(bars);
    }

    public ReadWriteArray<Bar> getBars() {
        return bars;
    }

    public void setBars(List<Bar> bars) {
        this.bars = new ReadWriteArray<>(Bar.class, bars);
    }
}
