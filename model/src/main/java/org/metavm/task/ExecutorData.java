package org.metavm.task;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Wire(24)
@Entity
public class ExecutorData extends org.metavm.entity.Entity {

    public static final IndexDef<ExecutorData> IDX_AVAIlABLE = IndexDef.create(ExecutorData.class,
            1, executorData -> List.of(Instances.booleanInstance(executorData.available)));
    public static final IndexDef<ExecutorData> IDX_IP = IndexDef.createUnique(ExecutorData.class,
            1, executorData -> List.of(Instances.stringInstance(executorData.ip)));

    private final String ip;
    @Setter
    private long lastHeartbeat;
    @Setter
    private boolean available;

    public ExecutorData(Id id, String ip) {
        super(id);
        this.ip = ip;
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
