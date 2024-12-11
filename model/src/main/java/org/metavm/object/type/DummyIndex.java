package org.metavm.object.type;

import org.metavm.api.Entity;

import java.util.List;

@Entity(ephemeral = true)
public class DummyIndex extends Index {

    public static final DummyIndex INSTANCE = new DummyIndex();

    private DummyIndex() {
        super(DummyKlass.INSTANCE, "unnamed", "", false, List.of(), null);
    }

    @Override
    void addField(IndexField item) {
    }

}
