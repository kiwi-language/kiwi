package org.metavm.autograph;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.entity.InstanceIndexQuery;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.IndexSource;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.InstanceIndexKey;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.util.CompilerHttpUtils;
import org.metavm.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalIndexSource implements IndexSource {

    public static final Logger logger = LoggerFactory.getLogger(LocalIndexSource.class);

    private CompilerInstanceContextFactory contextFactory;
    private final DiskTreeStore treeStore;
    private final LocalIndex index;

    public LocalIndexSource(long appId, DiskTreeStore treeStore, String indexDir) {
        Utils.ensureDirectoryExists(indexDir);
        this.treeStore = treeStore;
        this.index = new LocalIndex(appId, indexDir + File.separator + "index");
    }

    public void populateIndex() {
        try (var context = newContext()) {
            var ids = treeStore.getAllInstanceIds();
            Map<IndexKeyPO, String> indexMap = new HashMap<>();
            for (var id : ids) {
                context.get(id).forEachDescendant(instance -> {
                    if (instance.isEphemeral())
                        return;
                    if(instance instanceof ClassInstance classInstance) {
                        var keys = classInstance.getIndexKeys();
                        for (IndexKeyRT key : keys) {
                            indexMap.put(convertKey(key), instance.getStringId());
                        }
                    }
                });
            }
            index.reset(indexMap);
        }
    }

    private IndexKeyPO convertKey(IndexKeyRT indexKeyRT) {
        return new IndexKeyPO(indexKeyRT.getIndex().getId().toBytes(), indexKeyRT.getKeyBytes());
    }

    public IInstanceContext newContext() {
        return contextFactory.newEntityContext(CompilerHttpUtils.getAppId());
    }

    public LocalIndex.Query convertQuery(InstanceIndexQuery query) {
        return new LocalIndex.Query(query.index().getId(),
                Utils.safeCall(query.from(), InstanceIndexKey::toPO),
                Utils.safeCall(query.to(), InstanceIndexKey::toPO),
                query.desc(), query.limit());
    }

    @Override
    public List<Id> query(InstanceIndexQuery query, IInstanceContext context) {
        return Utils.map(index.query(convertQuery(query)).ids(), Id::parse);
    }

    @Override
    public long count(InstanceIndexQuery query, IInstanceContext context) {
        return index.query(convertQuery(query)).total();
    }

    @Override
    public long count(IndexKeyRT from, IndexKeyRT to, IInstanceContext context) {
        return index.count(from.toPO(), to.toPO());
    }

    @Override
    public List<Id> scan(IndexKeyRT from, IndexKeyRT to, IInstanceContext context) {
        return Utils.map(index.scan(from.toPO(), to.toPO()), Id::parse);
    }

    public void setContextFactory(CompilerInstanceContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }
}
