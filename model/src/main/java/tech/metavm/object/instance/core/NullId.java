package tech.metavm.object.instance.core;

import tech.metavm.util.InstanceOutput;

public class NullId extends Id {

    public static final byte[] BYTES = new byte[] {0};

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.NULL);
    }

    @Override
    public Long tryGetPhysicalId() {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return true;
    }
}
