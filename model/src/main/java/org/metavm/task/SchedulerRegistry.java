package org.metavm.task;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire(34)
@Entity
public class SchedulerRegistry extends org.metavm.entity.Entity {

    public static final IndexDef<SchedulerRegistry> IDX_ALL_FLAG = IndexDef.create(SchedulerRegistry.class, 1,
            e -> List.of(Instances.booleanInstance(e.allFlag)));

    public static final long HEARTBEAT_TIMEOUT = 20000000000L;

    public static SchedulerRegistry getInstance(IInstanceContext context) {
        return Objects.requireNonNull(context.selectFirstByKey(IDX_ALL_FLAG, Instances.trueInstance()), "SchedulerRegistry not initialized");
    }

    public static void initialize(IInstanceContext context) {
        var existing = context.selectFirstByKey(SchedulerRegistry.IDX_ALL_FLAG, Instances.trueInstance());
        if (existing != null)
            throw new IllegalStateException("SchedulerRegistry already exists");
        context.bind(new SchedulerRegistry(context.allocateRootId()));
    }

    private long version;
    @Setter
    @Getter
    private long lastHeartbeat;
    @Getter
    @Setter
    @Nullable
    private String ip;
    private final boolean allFlag = true;

    public SchedulerRegistry(Id id) {
        super(id);
    }

    public boolean isHeartbeatTimeout() {
        return System.currentTimeMillis() - lastHeartbeat > HEARTBEAT_TIMEOUT;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}