package tech.metavm.entity;

import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.type.*;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class DefaultIdInitializer implements IdInitializer {

    private final EntityIdProvider idProvider;

    public DefaultIdInitializer(EntityIdProvider idProvider) {
        this.idProvider = idProvider;
    }

    @Override
    public void initializeIds(long appId, Collection<? extends DurableInstance> instancesToInitId) {
        var classTypeType = ModelDefRegistry.getType(ClassType.class);
        var countMap = Map.of(classTypeType, (int) NncUtils.count(instancesToInitId, DurableInstance::isRoot));
        var ids = new LinkedList<>(idProvider.allocate(appId, countMap).get(classTypeType));
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
            inst.initId(new PhysicalId(treeId, nodeId, type.getTypeTag()));
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
