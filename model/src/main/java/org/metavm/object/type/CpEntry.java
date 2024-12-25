package org.metavm.object.type;

import org.metavm.api.ValueObject;
import org.metavm.entity.Entity;
import org.metavm.util.MvOutput;

public abstract class CpEntry extends Entity implements ValueObject {

    private final int index;

    protected CpEntry(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public abstract Object getValue();

    public abstract void write(MvOutput output);

}
