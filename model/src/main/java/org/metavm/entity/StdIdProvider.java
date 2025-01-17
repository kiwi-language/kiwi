package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.type.StdAllocators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StdIdProvider {

    public static final Logger logger = LoggerFactory.getLogger(StdIdProvider.class);

    private final StdAllocators allocators;
//    private final Map<String, Id> ids;
//    private final Map<Id, String> qualifiedNames;

    public StdIdProvider(StdAllocators allocators) {
        this.allocators = allocators;
//        ids = stdIdStore.load();
//        qualifiedNames = new HashMap<>();
//        ids.forEach((qualName, id) -> qualifiedNames.put(id, qualName));
//        logger.info("loaded {} ids", ids.size());
    }

    public Id getId(ModelIdentity modelIdentity) {
//        return ids.get(modelIdentity.qualifiedName());
        return allocators.getId(modelIdentity);
    }

//    public String getName(Id id) {
//        return qualifiedNames.get(id).split("/")[1];
//    }

//    public void save(Map<ModelIdentity, Id> ids) {
//        var qualName2Id = new HashMap<String, Id>();
//        for (var entry : ids.entrySet()) {
//            qualName2Id.put(entry.getKey().qualifiedName(), entry.getValue());
//        }
//        this.ids.clear();
//        this.qualifiedNames.clear();
//        this.ids.putAll(qualName2Id);
//        this.ids.forEach((qualName, id) -> qualifiedNames.put(id, qualName));
//        stdIdStore.save(qualName2Id);
//    }

}
