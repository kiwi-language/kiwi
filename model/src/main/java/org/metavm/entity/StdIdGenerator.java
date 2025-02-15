package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.natives.StdFunction;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.util.DebugEnv;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class StdIdGenerator {

    private final Supplier<Long> allocator;
    private final Map<ModelIdentity, Id> ids = new HashMap<>();
    private final Map<ModelIdentity, Long> nextNodeIds = new HashMap<>();
    private final Set<Class<?>> visited = new HashSet<>();

    public StdIdGenerator(Supplier<Long> allocator) {
        this.allocator = allocator;
    }

    public void generate() {
        for (Class<?> c : StandardDefBuilder.classes) {
            generate(c);
        }
        for (Class<?> c : EntityUtils.getModelClasses()) {
            parsingGenerate(c);
        }
        for (StdKlass value : StdKlass.values()) {
            parsingGenerate(value.getJavaClass());
        }
        for (StdFunction value : StdFunction.values()) {
            FunctionIdGenerator.initFunctionId(
                    value.getSignature(), allocator.get(), ids::put, nextNodeIds::put
            );
        }
    }

    private void generate(Class<?> clazz) {
        clazz = getOutermostClass(clazz);
        if (visited.add(clazz))
            new ReflectIdGenerator(clazz, allocator.get(), ids::put, this::collectRootId, this::generate).generate();
    }

    private void parsingGenerate(Class<?> clazz) {
        clazz = getOutermostClass(clazz);
        if (visited.add(clazz)) {
            new ParsingIdGenerator(
                    clazz, allocator.get(), ids::put, this::collectRootId, this::parsingGenerate
            ).generate();
        }
    }

    private Class<?> getOutermostClass(Class<?> clazz) {
        while (clazz.getDeclaringClass() != null) clazz = clazz.getDeclaringClass();
        return clazz;
    }

    private void collectRootId(ModelIdentity identity, long treeId, long nextNodeId) {
        ids.put(identity, PhysicalId.of(treeId, 0L));
        nextNodeIds.put(identity, nextNodeId);
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

    public void printIds() {
        ids.forEach((identity, id) -> DebugEnv.logger.trace("{}: {}", identity, id));
    }


}
