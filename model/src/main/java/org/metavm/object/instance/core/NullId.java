package org.metavm.object.instance.core;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;

public class NullId extends Id {

    public NullId() {
        super();
    }

    @Override
    public void write(MvOutput output) {
        output.writeIdTag(IdTag.NULL);
    }

    @Override
    public Long tryGetTreeId() {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return true;
    }

    @Override
    public int getTypeTag(TypeDefProvider typeDefProvider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo0(Id id) {
        return 0;
    }

    @Override
    public int getTag() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullId;
    }
}
