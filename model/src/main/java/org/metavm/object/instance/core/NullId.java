package org.metavm.object.instance.core;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;

public class NullId extends Id {

    public static final byte[] BYTES = new byte[] {0};

    public NullId() {
        super();
    }

    @Override
    public void write(MvOutput output) {
        output.writeIdTag(IdTag.NULL, false);
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
}
