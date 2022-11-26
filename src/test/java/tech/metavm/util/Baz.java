package tech.metavm.util;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import java.util.List;

@EntityType("巴子")
public class Baz {

    @ChildEntity("巴巴巴巴")
    private List<Bar> bars;

    public List<Bar> getBars() {
        return bars;
    }

    public void setBars(List<Bar> bars) {
        this.bars = bars;
    }
}
