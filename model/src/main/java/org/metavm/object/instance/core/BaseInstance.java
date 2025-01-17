package org.metavm.object.instance.core;

import org.metavm.entity.NoProxy;

import javax.annotation.Nullable;

public abstract class BaseInstance implements Instance {

    protected transient InstanceState state;

    public BaseInstance(@Nullable Id id, long version, long syncVersion, boolean ephemeral) {
        state = new InstanceState(id, version, syncVersion, ephemeral, this);
    }

    @Override
    public InstanceState state() {
        return state;
    }

    @NoProxy
    public void initState(Id id, long version ,long syncVersion, boolean ephemeral) {
        state = new InstanceState(id, version, syncVersion, ephemeral, this);
    }

}
