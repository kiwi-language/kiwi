package tech.metavm.entity;

import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.util.IdentitySet;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class DefaultIdInitializer implements IdInitializer {

    private final EntityIdProvider idProvider;

    public DefaultIdInitializer(EntityIdProvider idProvider) {
        this.idProvider = idProvider;
    }

    @Override
    public TypeId getTypeId(Id id) {
        return idProvider.getTypeId(id);
    }

    @Override
    public void initializeIds(long appId, Collection<? extends DurableInstance> instancesToInitId) {
        var countMap = Map.of(ModelDefRegistry.getType(ClassType.class), instancesToInitId.size());
        var ids = new LinkedList<>(idProvider.allocate(appId, countMap).get(ModelDefRegistry.getType(ClassType.class)));
        var classType = ModelDefRegistry.getType(ClassType.class);
        var arrayType = ModelDefRegistry.getType(ArrayType.class);
        var fieldType = ModelDefRegistry.getType(Field.class);
        Map<Type, DurableInstance> typeInstance = new HashMap<>();
        for (DurableInstance instance : instancesToInitId) {
            if (instance.getMappedEntity() instanceof Type type)
                typeInstance.put(type, instance);
        }
        var sortedInstances = sort(instancesToInitId, typeInstance);
        for (DurableInstance inst : sortedInstances) {
            var type = inst.getType();
            IdTag tag;
            if (type == classType)
                tag = IdTag.CLASS_TYPE_PHYSICAL;
            else if (type == arrayType)
                tag = IdTag.ARRAY_TYPE_PHYSICAL;
            else if (type == fieldType)
                tag = IdTag.FIELD_PHYSICAL;
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
                var typeId = typeInstance.containsKey(type) ? typeInstance.get(type).getId() : type.getId();
                inst.initId(new DefaultPhysicalId(inst instanceof ArrayInstance, treeId, nodeId, typeId));
            }
        }
    }

    private List<DurableInstance> sort(Collection<? extends DurableInstance> instances, Map<Type, DurableInstance> typeInstance) {
        var result = new ArrayList<DurableInstance>();
        var visited = new IdentitySet<DurableInstance>();
        var visiting = new IdentitySet<DurableInstance>();
        instances.forEach(i -> visit(i, result, visited, visiting, typeInstance));
        return result;
    }

    private void visit(DurableInstance instance, List<DurableInstance> result, Set<DurableInstance> visited,
                       Set<DurableInstance> visiting, Map<Type, DurableInstance> type2instance) {
        if(visited.contains(instance))
            return;
        if(visiting.contains(instance))
            throw new IllegalArgumentException("Cycle detected");
        visiting.add(instance);
        var typeInst = type2instance.get(instance.getType());
        if(typeInst != null && typeInst != instance)
            visit(typeInst, result, visited, visiting, type2instance);
        if(!instance.isRoot() && instance.getRoot().tryGetPhysicalId() == null)
            visit(instance.getRoot(), result, visited, visiting, type2instance);
        visiting.remove(instance);
        visited.add(instance);
        result.add(instance);
    }

}
