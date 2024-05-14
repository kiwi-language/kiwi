package tech.metavm.autograph;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.InstanceIndexQueryItem;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.util.BytesUtils;
import tech.metavm.util.CompilerHttpUtils;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalIndexSource implements IndexSource {

    private CompilerInstanceContextFactory contextFactory;
    private final DiskTreeStore treeStore;
    private final LocalIndex index;

    public LocalIndexSource(long appId, DiskTreeStore treeStore, String indexDir) {
        NncUtils.ensureDirectoryExists(indexDir);
        this.treeStore = treeStore;
        this.index = new LocalIndex(appId, indexDir + File.separator + "index");
    }

    public void populateIndex() {
        try (var context = newContext()) {
            var ids = treeStore.getAllInstanceIds();
            var instanceContext = context.getInstanceContext();
            Map<IndexKeyPO, String> indexMap = new HashMap<>();
            for (var id : ids) {
                instanceContext.get(id).accept(new StructuralVisitor() {
                    @Override
                    public Void visitClassInstance(ClassInstance instance) {
                        if (instance.isEphemeral())
                            return null;
                        var keys = instance.getIndexKeys();
                        for (IndexKeyRT key : keys) {
                            indexMap.put(convertKey(key), instance.getStringId());
                        }
                        return super.visitClassInstance(instance);
                    }
                });
            }
            index.reset(indexMap);
        }
    }

    private IndexKeyPO convertKey(IndexKeyRT indexKeyRT) {
        var fields = indexKeyRT.getIndex().getFields();
        byte[][] bytes = new byte[IndexKeyPO.MAX_KEY_COLUMNS][];
        for (int i = 0; i < IndexKeyPO.MAX_KEY_COLUMNS; i++) {
            Instance value;
            if (i < fields.size())
                value = indexKeyRT.getField(fields.get(i));
            else
                value = Instances.nullInstance();
            bytes[i] = BytesUtils.toIndexBytes(value);
        }
        return new IndexKeyPO(indexKeyRT.getIndex().getId().toBytes(), bytes);
    }

    public IEntityContext newContext() {
        return contextFactory.newEntityContext(CompilerHttpUtils.getAppId());
    }

    public LocalIndex.Query convertQuery(InstanceIndexQuery query) {
        return new LocalIndex.Query(query.index().getId(),
                NncUtils.get(query.from(), InstanceIndexKey::toPO),
                NncUtils.get(query.to(), InstanceIndexKey::toPO),
                query.desc(), query.limit());
    }

    public LocalIndex.QueryItem convertQueryItem(InstanceIndexQueryItem queryItem) {
        return new LocalIndex.QueryItem(
                queryItem.operator(),
                BytesUtils.toIndexBytes(queryItem.value())
        );
    }

    @Override
    public List<Id> query(InstanceIndexQuery query, IInstanceContext context) {
        return NncUtils.map(index.query(convertQuery(query)).ids(), Id::parse);
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
        return NncUtils.map(index.scan(from.toPO(), to.toPO()), Id::parse);
    }

    public void setContextFactory(CompilerInstanceContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }
}
