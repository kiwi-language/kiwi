package org.metavm.object.type;

import org.metavm.flow.KlassOutput;

public class IndexCpEntry extends CpEntry {

    private final IndexRef indexRef;

    public IndexCpEntry(int index, IndexRef indexRef) {
        super(index);
        this.indexRef = indexRef;
    }

    @Override
    public IndexRef getValue() {
        return indexRef;
    }

    @Override
    public Object resolve() {
        return indexRef.resolve();
    }

    @Override
    public void write(KlassOutput output) {
        indexRef.write(output);
    }
}
