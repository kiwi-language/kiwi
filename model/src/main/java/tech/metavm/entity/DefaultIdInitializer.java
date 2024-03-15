package tech.metavm.entity;

import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.*;

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
        var countMap = NncUtils.mapAndCount(instancesToInitId, Instance::getType);
        var type2ids = idProvider.allocate(appId, countMap);
        var classType = ModelDefRegistry.getType(ClassType.class);
        var classTypeInst = ModelDefRegistry.getDefContext().getInstance(classType);
        Map<Type, DurableInstance> typeInstance = new HashMap<>();
        if(instancesToInitId.remove(classTypeInst)) {
            var ids = type2ids.get(classType);
            var id = ids.remove(ids.size() - 1);
            classTypeInst.initId(PhysicalId.ofClass(id, id));
            typeInstance.put(classType, classTypeInst);
        }
        for (DurableInstance instance : instancesToInitId) {
            if(instance.getMappedEntity() instanceof Type type)
                typeInstance.put(type, instance);
        }
        var type2instances = NncUtils.toMultiMap(instancesToInitId, Instance::getType);
        var arrayType = ModelDefRegistry.getType(ArrayType.class);
        var types = new ArrayList<>(type2instances.keySet());
        types.sort(Comparator.comparingInt(t -> {
            if(t == classType)
                return 0;
            if(t == arrayType)
                return 1;
            return 2;
        }));
        for (Type type : types) {
            var instances = type2instances.get(type);
            var ids = type2ids.get(type);
            var typeTag = type.getTag();
            var typeId = typeInstance.containsKey(type) ? typeInstance.get(type).getPhysicalId() : type.getPhysicalId();
            NncUtils.biForEach(instances, ids, (inst, id) -> inst.initId(PhysicalId.of(id, typeTag, typeId)));
        }
    }
}
