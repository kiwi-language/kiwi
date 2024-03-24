package tech.metavm.object.instance.core;

import tech.metavm.util.InstanceOutput;

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
    public Long tryGetPhysicalId() {
        return null;
    }

    @Override
    public boolean isTemporary() {
        return true;
    }
}
