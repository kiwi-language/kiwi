package tech.metavm.object.instance.core;

import tech.metavm.util.InstanceOutput;

public class MockId extends Id {

    private final long id;

    public MockId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public void write(InstanceOutput output) {
        output.writeIdTag(IdTag.MOCK);
        output.writeLong(id);
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
