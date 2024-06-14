package org.metavm.object.instance.core;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.view.MappingProvider;
import org.metavm.util.InstanceOutput;

public class NullId extends Id {

    public static final byte[] BYTES = new byte[] {0};

    public NullId() {
        super(false);
    }

    @Override
    public void write(InstanceOutput output) {
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
    public int getTypeTag(MappingProvider mappingProvider, TypeDefProvider typeDefProvider) {
        throw new UnsupportedOperationException();
    }
}
