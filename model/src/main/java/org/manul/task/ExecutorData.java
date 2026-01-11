package org.manul.task;

import lombok.Getter;
import lombok.Setter;
import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.entity.IndexDef;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Wire(24)
@Entity
public class ExecutorData extends org.manul.entity.Entity {

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
    public org.manul.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
