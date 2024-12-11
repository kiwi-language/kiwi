package org.metavm.mocks;

import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.ReadWriteArray;

import java.util.List;

@Entity
public class Baz extends org.metavm.entity.Entity {

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
