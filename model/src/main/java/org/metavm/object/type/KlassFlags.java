package org.metavm.object.type;

import org.metavm.api.Entity;

@Entity(since = 1)
public class KlassFlags extends org.metavm.entity.Entity {
    private boolean flag1;

    public boolean isFlag1() {
        return flag1;
    }

    public void setFlag1(boolean flag1) {
        this.flag1 = flag1;
    }
}
