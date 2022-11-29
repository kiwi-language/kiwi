package tech.metavm.util;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ValueType;

import java.util.List;

@ValueType("巴子")
public class Baz {

    @ChildEntity("巴巴巴巴")
    private Table<Bar> bars;

    public List<Bar> getBars() {
        return bars;
    }

    public void setBars(List<Bar> bars) {
        this.bars = new Table<>(bars);
    }
}
