package tech.metavm.entity;

import java.util.HashMap;
import java.util.Map;

public class StdIdProvider {

    private final StdIdStore stdIdStore;
    private final Map<String, Long> ids;

    public StdIdProvider(StdIdStore stdIdStore) {
        this.stdIdStore = stdIdStore;
        ids = stdIdStore.load();
    }

    public Long getId(ModelIdentity modelIdentity) {
        return ids.get(modelIdentity.qualifiedName());
    }

    public void save(Map<ModelIdentity, Long> ids) {
        var qualName2Id = new HashMap<String, Long>();
        for (var entry : ids.entrySet()) {
            qualName2Id.put(entry.getKey().qualifiedName(), entry.getValue());
        }
        this.ids.clear();
        this.ids.putAll(qualName2Id);
        stdIdStore.save(qualName2Id);
    }

}
