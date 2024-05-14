package tech.metavm.entity;

import tech.metavm.object.instance.core.*;
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
        var arrayTypeType = ModelDefRegistry.getType(ArrayType.class);
        var fieldType = ModelDefRegistry.getType(Field.class);
        var klassType = ModelDefRegistry.getType(Klass.class);
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
            IdTag tag;
            if (type.equals(classTypeType))
                tag = IdTag.CLASS_TYPE_PHYSICAL;
            else if (type.equals(arrayTypeType))
                tag = IdTag.ARRAY_TYPE_PHYSICAL;
            else if (type.equals(fieldType))
                tag = IdTag.FIELD_PHYSICAL;
            else if (type.equals(klassType))
                tag = IdTag.KLASS_PHYSICAL;
            else
                tag = null;
            long treeId, nodeId;
            if(inst.isRoot()) {
                treeId = requireNonNull(ids.poll());
                nodeId = 0;
            } else {
                var root = inst.getRoot();
                treeId = root.getPhysicalId();
                nodeId = root.nextNodeId();
            }
            if (tag != null)
                inst.initId(new TaggedPhysicalId(tag, treeId, nodeId));
            else {
                var typeKey = type.toTypeKey(typeDef -> NncUtils.getOrElse(typeDefInstance.get(typeDef), DurableInstance::getStringId, typeDef.getStringId()));
                inst.initId(new DefaultPhysicalId(inst instanceof ArrayInstance, treeId, nodeId, typeKey));
            }
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
        if(visited.contains(instance))
            return;
        if(visiting.contains(instance))
            throw new IllegalArgumentException("Cycle detected, path: " + (path != null ? NncUtils.join(path, Instances::getInstanceDesc, "->") : "null"));
        if(path != null)
            path.add(instance);
        visiting.add(instance);
        instance.getType().forEachTypeDef(typeDef -> {
            var typeInst = typeDef2instance.get(typeDef);
            if(typeInst != null && typeInst != instance)
                visit(typeInst, result, visited, visiting, typeDef2instance, path);
        });
        if(!instance.isRoot() && instance.getRoot().tryGetPhysicalId() == null)
            visit(instance.getRoot(), result, visited, visiting, typeDef2instance, path);
        visiting.remove(instance);
        visited.add(instance);
        result.add(instance);
        if(path != null)
            path.removeLast();
    }

}
