package tech.metavm.util;

import tech.metavm.entity.*;

import java.util.Collection;
import java.util.List;

@EntityType("巴子")
public class Baz extends Entity {

    @ChildEntity("巴巴巴巴")
    private Table<Bar> bars = new Table<>();

    public Baz() {
    }

    public Baz(List<Bar> bars) {
        setBars(bars);
    }

    public Table<Bar> getBars() {
        return bars;
    }

    public void setBars(List<Bar> bars) {
        this.bars = new Table<>(bars);
    }
}
