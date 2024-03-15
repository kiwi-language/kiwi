package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.Id;

import java.util.HashMap;
import java.util.Map;

public class StdIdProvider {

    public static final Logger LOGGER = LoggerFactory.getLogger(StdIdProvider.class);

    private final StdIdStore stdIdStore;
    private final Map<String, Id> ids;

    public StdIdProvider(StdIdStore stdIdStore) {
        this.stdIdStore = stdIdStore;
        ids = stdIdStore.load();
        LOGGER.info("loaded {} ids", ids.size());
    }

    public Id getId(ModelIdentity modelIdentity) {
        return ids.get(modelIdentity.qualifiedName());
    }

    public void save(Map<ModelIdentity, Id> ids) {
        var qualName2Id = new HashMap<String, Id>();
        for (var entry : ids.entrySet()) {
            qualName2Id.put(entry.getKey().qualifiedName(), entry.getValue());
        }
        this.ids.clear();
        this.ids.putAll(qualName2Id);
        stdIdStore.save(qualName2Id);
    }

}
