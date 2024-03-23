package tech.metavm.entity;

import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
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
        var arrayType = ModelDefRegistry.getType(ArrayType.class);
        var fieldType = ModelDefRegistry.getType(Field.class);
        Map<Type, DurableInstance> typeInstance = new HashMap<>();
//        if(instancesToInitId.remove(classTypeInst)) {
//            var ids = type2ids.get(classType);
//            var id = ids.remove(ids.size() - 1);
//            classTypeInst.initId(PhysicalId.of(id, id));
//            typeInstance.put(classType, classTypeInst);
//        }
        for (DurableInstance instance : instancesToInitId) {
            if(instance.getMappedEntity() instanceof Type type)
                typeInstance.put(type, instance);
        }
        var type2instances = NncUtils.toMultiMap(instancesToInitId, Instance::getType);

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
            IdTag tag;
            if(type == classType)
                tag = IdTag.CLASS_TYPE_PHYSICAL;
            else if(type == arrayType)
                tag = IdTag.ARRAY_TYPE_PHYSICAL;
            else if(type == fieldType)
                tag = IdTag.FIELD_PHYSICAL;
            else
                tag = null;
            if(tag != null)
                NncUtils.biForEach(instances, ids, (inst, id) -> inst.initId(new TaggedPhysicalId(tag, id, 0)));
            else {
                var typeId = typeInstance.containsKey(type) ? typeInstance.get(type).getId() : type.getId();
                NncUtils.biForEach(instances, ids, (inst, id) -> inst.initId(DefaultPhysicalId.of(id, 0, typeId)));
            }
        }
    }
}
