package org.metavm.entity;

import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.StructuralVisitor;
import org.metavm.object.type.TypeDef;
import org.metavm.util.DebugEnv;
import org.metavm.util.IdentitySet;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class DefaultIdInitializer implements IdInitializer {

    private final EntityIdProvider idProvider;

    public static final Logger logger = LoggerFactory.getLogger(DefaultIdInitializer.class);

    public DefaultIdInitializer(EntityIdProvider idProvider) {
        this.idProvider = idProvider;
    }

    @Override
    public void initializeIds(long appId, Collection<? extends DurableInstance> instancesToInitId) {
        if (DebugEnv.instance != null) {
            logger.info("treeId: {}, next nodeId: {}",
                    DebugEnv.instance.isIdInitialized() ? DebugEnv.instance.getId().getTreeId() : null,
                    DebugEnv.instance.getRoot().getNextNodeId());
            DebugEnv.instance.accept(new StructuralVisitor() {
                @Override
                public Void visitDurableInstance(DurableInstance instance) {
                    if (instance.isIdInitialized()) {
                        logger.info("child instance: {}, id: {}, treeId: {}, nodeId: {}",
                                Instances.getInstancePath(instance),
                                instance.getId(),
                                instance.getId().getTreeId(),
                                instance.getId().getNodeId());
                    }
                    return super.visitDurableInstance(instance);
                }
            });
        }
        var klassType = StdKlass.entity.type();
        var countMap = Map.of(klassType, (int) NncUtils.count(instancesToInitId, DurableInstance::isRoot));
        var ids = new LinkedList<>(idProvider.allocate(appId, countMap).get(klassType));
        var typeDefInstance = new HashMap<TypeDef, DurableInstance>();
        for (DurableInstance instance : instancesToInitId) {
            if (instance.getMappedEntity() instanceof TypeDef typeDef)
                typeDefInstance.put(typeDef, instance);
        }
        var sortedInstances = sort(instancesToInitId, typeDefInstance);
        for (DurableInstance inst : sortedInstances) {
            var type = inst.getType();
            long treeId, nodeId;
            if (inst.isRoot()) {
                treeId = requireNonNull(ids.poll());
                nodeId = 0;
            } else {
                var root = inst.getRoot();
                treeId = root.getTreeId();
                nodeId = root.nextNodeId();
            }
            inst.initId(PhysicalId.of(treeId, nodeId, type));
        }
    }

    private List<DurableInstance> sort(Collection<? extends DurableInstance> instances, Map<TypeDef, DurableInstance> typeInstance) {
        var result = new ArrayList<DurableInstance>();
        var visited = new IdentitySet<DurableInstance>();
        var visiting = new IdentitySet<DurableInstance>();
        var path = DebugEnv.recordPath ? new LinkedList<DurableInstance>() : null;
        instances.forEach(i -> visit(i, result, visited, visiting, typeInstance, path));
        return result;
    }

    private void visit(DurableInstance instance, List<DurableInstance> result, Set<DurableInstance> visited,
                       Set<DurableInstance> visiting, Map<TypeDef, DurableInstance> typeDef2instance, @Nullable LinkedList<DurableInstance> path) {
        if (visited.contains(instance))
            return;
        if (visiting.contains(instance))
            throw new IllegalArgumentException("Cycle detected, path: " + (path != null ? NncUtils.join(path, Instances::getInstanceDesc, "->") : "null"));
        if (path != null)
            path.add(instance);
        visiting.add(instance);
        instance.getType().forEachTypeDef(typeDef -> {
            var typeInst = typeDef2instance.get(typeDef);
            if (typeInst != null && typeInst != instance)
                visit(typeInst, result, visited, visiting, typeDef2instance, path);
        });
        if (!instance.isRoot() && instance.getRoot().tryGetTreeId() == null)
            visit(instance.getRoot(), result, visited, visiting, typeDef2instance, path);
        visiting.remove(instance);
        visited.add(instance);
        result.add(instance);
        if (path != null)
            path.removeLast();
    }

}
