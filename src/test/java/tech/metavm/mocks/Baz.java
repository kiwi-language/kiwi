package tech.metavm.mocks;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("巴子")
public class Baz extends Entity {

    @ChildEntity("巴巴巴巴")
    private Table<Bar> bars = new Table<>(Bar.class);

    public Baz() {
    }

    public Baz(List<Bar> bars) {
        setBars(bars);
    }

    public Table<Bar> getBars() {
        return bars;
    }

    public void setBars(List<Bar> bars) {
        this.bars = new Table<>(Bar.class, bars);
    }
}
