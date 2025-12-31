package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.StdFunction;
import org.metavm.object.instance.core.Id;
import org.metavm.util.DebugEnv;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class StdIdGenerator {

    private final Supplier<Long> allocator;
    private final Map<ModelIdentity, Id> ids = new HashMap<>();
    private final Map<ModelIdentity, Long> nextNodeIds = new HashMap<>();

    public StdIdGenerator(Supplier<Long> allocator) {
        this.allocator = allocator;
    }

    public void generate() {
        for (StdFunction value : StdFunction.values()) {
            FunctionIdGenerator.initFunctionId(
                    value.getSignature(), allocator.get(), ids::put, nextNodeIds::put
            );
        }
    }

    public Id getId(Object o) {
        var identity = o instanceof ModelIdentity i ? i : ModelIdentities.getIdentity(o);
        return ids.get(identity);
    }

    public Map<ModelIdentity, Id> getIds() {
        return Collections.unmodifiableMap(ids);
    }

    public long getNextNodeId(Object o) {
        var identity = o instanceof ModelIdentity i ? i : ModelIdentities.getIdentity(o);
        return nextNodeIds.get(identity);
    }

    /** @noinspection unused*/
    public void printIds() {
        ids.forEach((identity, id) -> DebugEnv.logger.trace("{}: {}", identity, id));
    }


}
