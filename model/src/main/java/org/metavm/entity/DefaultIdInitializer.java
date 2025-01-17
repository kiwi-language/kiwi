package org.metavm.entity;

import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.type.TypeDef;
import org.metavm.util.DebugEnv;
import org.metavm.util.IdentitySet;
import org.metavm.util.Instances;
import org.metavm.util.Utils;
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
    public void initializeIds(long appId, Collection<? extends Instance> instancesToInitId) {
        var count = (int) Utils.count(instancesToInitId, Instance::isRoot);
        var ids = new LinkedList<>(idProvider.allocate(appId, count));
        var typeDefInstance = new HashMap<TypeDef, Instance>();
        for (Instance instance : instancesToInitId) {
            if (instance instanceof TypeDef typeDef)
                typeDefInstance.put(typeDef, instance);
        }
        var sortedInstances = sort(instancesToInitId, typeDefInstance);
        for (Instance inst : sortedInstances) {
            var type = inst.getInstanceType();
            long treeId, nodeId;
            if (inst.isRoot()) {
                treeId = requireNonNull(ids.poll());
                nodeId = 0;
            } else {
                var root = inst.getRoot();
                treeId = root.getTreeId();
                nodeId = root.nextNodeId();
            }
            inst.initId(PhysicalId.of(treeId, nodeId));
        }
    }

    private List<Instance> sort(Collection<? extends Instance> instances, Map<TypeDef, Instance> typeInstance) {
        var result = new ArrayList<Instance>();
        var visited = new IdentitySet<Instance>();
        var visiting = new IdentitySet<Instance>();
        var path = DebugEnv.recordPath ? new LinkedList<Instance>() : null;
        instances.forEach(i -> visit(i, result, visited, visiting, typeInstance, path));
        return result;
    }

    private void visit(Instance instance, List<Instance> result, Set<Instance> visited,
                       Set<Instance> visiting, Map<TypeDef, Instance> typeDef2instance, @Nullable LinkedList<Instance> path) {
        if (visited.contains(instance))
            return;
        if (visiting.contains(instance))
            throw new IllegalArgumentException("Cycle detected, path: " + (path != null ? Utils.join(path, Instances::getInstanceDesc, "->") : "null"));
        if (path != null)
            path.add(instance);
        visiting.add(instance);
        instance.getInstanceType().forEachTypeDef(typeDef -> {
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
