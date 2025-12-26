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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Wire(45)
@Entity
public class ShadowTask extends org.metavm.entity.Entity {

    public static final Logger logger = LoggerFactory.getLogger(ShadowTask.class);

    public static final IndexDef<ShadowTask> IDX_RUN_AT = IndexDef.create(ShadowTask.class,
            1, shadowTask -> List.of(Instances.longInstance(shadowTask.runAt)));
    public static final IndexDef<ShadowTask> IDX_EXECUTOR_IP_START_AT = IndexDef.create(ShadowTask.class,
            2, shadowTask ->
                    List.of(
                            shadowTask.executorIP != null ?
                                    Instances.stringInstance(shadowTask.executorIP) : Instances.nullInstance(),
                            Instances.longInstance(shadowTask.startAt)
                    ));

    @Setter
    @Getter
    private TaskState state = TaskState.RUNNABLE;
    private @Nullable String executorIP;
    private final long startAt;
    @Setter
    @Getter
    private long runAt;
    @Getter
    private final long appId;
    @Getter
    private final Id appTaskId;
    public static BiConsumer<Long, List<Task>> saveShadowTasksHook;

    public ShadowTask(Id id, long appId, Id appTaskId, long startAt) {
        super(id);
        this.appId = appId;
        this.appTaskId = appTaskId;
        this.startAt = startAt;
    }

    @Nullable
    public String getExecutorIP() {
        return executorIP;
    }

    public void setExecutorIP(@Nullable String executorIP) {
        this.executorIP = executorIP;
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