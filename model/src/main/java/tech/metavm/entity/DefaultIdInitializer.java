package tech.metavm.entity;

import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

public class DefaultIdInitializer implements IdInitializer {

    private final EntityIdProvider idProvider;

    public DefaultIdInitializer(EntityIdProvider idProvider) {
        this.idProvider = idProvider;
    }

    @Override
    public long getTypeId(long id) {
        return idProvider.getTypeId(id);
    }

    @Override
    public void initializeIds(long appId, Collection<? extends DurableInstance> instancesToInitId) {
        var countMap = NncUtils.mapAndCount(instancesToInitId, Instance::getType);
        var type2ids = idProvider.allocate(appId, countMap);
        var type2instances = NncUtils.toMultiMap(instancesToInitId, Instance::getType);
//        var allocatedMap = new HashMap<Long, Instance>();
        type2instances.forEach((type, instances) -> {
            List<Long> ids = type2ids.get(type);
//            for (Long id : ids) {
//                boolean contains1 = allocatedMap.containsKey(id);
//                if (contains1)
//                    throw new InternalException();
//                boolean contains = containsId(id);
//                if (contains)
//                    throw new InternalException();
//            }
//            for (var instance : instances) {
//                allocatedMap.put(instance.tryGetPhysicalId(), instance);
//            }
            NncUtils.biForEach(instances, ids, (inst, id) -> inst.initId(PhysicalId.of(id)));
        });
    }
}
