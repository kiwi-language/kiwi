package tech.metavm.autograph;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.InstanceIndexQueryItem;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.type.IndexField;
import tech.metavm.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LocalIndexSource implements IndexSource {

    private CompilerInstanceContextFactory contextFactory;
    private final DiskTreeStore treeStore;
    private final LocalIndex index;
    private final LocalTypeIndex typeIndex;

    public LocalIndexSource(DiskTreeStore treeStore, String indexDir) {
        NncUtils.ensureDirectoryExists(indexDir);
        this.treeStore = treeStore;
        this.index = new LocalIndex(indexDir + File.separator + "index");
        this.typeIndex = new LocalTypeIndex(indexDir + File.separator + "type_index");
    }

    public void populateIndex() {
        try (var context = newContext()) {
            var ids = treeStore.getAllInstanceIds();
            var instanceContext = context.getInstanceContext();
            Map<IndexKeyPO, String> indexMap = new HashMap<>();
            Map<Long, List<Long>> typeId2ids = new HashMap<>();
            for (var id : ids) {
                instanceContext.get(id).accept(new StructuralVisitor() {
                    @Override
                    public Void visitClassInstance(ClassInstance instance) {
                        if (instance.isEphemeral())
                            return null;
                        typeId2ids.computeIfAbsent(instance.getType().getPhysicalId(), k -> new ArrayList<>()).add(instance.getPhysicalId());
                        var keys = instance.getIndexKeys(context.getGenericContext());
                        for (IndexKeyRT key : keys) {
                            indexMap.put(convertKey(key), instance.getStringId());
                        }
                        return super.visitClassInstance(instance);
                    }
                });
            }
            index.reset(indexMap);
            typeIndex.reset(typeId2ids);
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
        var itemMap = NncUtils.toMap(query.items(), InstanceIndexQueryItem::field, Function.identity());
        List<LocalIndex.QueryItem> items = new ArrayList<>();
        for (IndexField field : query.index().getFields()) {
            var queryItem = itemMap.get(field);
            items.add(convertQueryItem(queryItem));
        }
        return new LocalIndex.Query(query.index().getId(), items, query.desc(), query.limit());
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
