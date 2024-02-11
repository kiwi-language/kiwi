package tech.metavm.autograph;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.entity.InstanceIndexQueryItem;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.type.Index;
import tech.metavm.object.type.IndexField;
import tech.metavm.util.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
            Map<IndexKeyPO, Long> indexMap = new HashMap<>();
            Map<Long, List<Long>> typeId2ids = new HashMap<>();
            for (Long id : ids) {
                instanceContext.get(PhysicalId.of(id)).accept(new StructuralVisitor() {
                    @Override
                    public Void visitClassInstance(ClassInstance instance) {
                        if (instance.isEphemeral())
                            return null;
                        typeId2ids.computeIfAbsent(instance.getType().tryGetId(), k -> new ArrayList<>()).add(instance.getPhysicalId());
                        var keys = instance.getIndexKeys(context.getGenericContext());
                        for (IndexKeyRT key : keys) {
                            indexMap.put(convertKey(key), instance.getPhysicalId());
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
        byte[][] bytes = new byte[CompilerConstants.NUM_INDEX_COLS][];
        for (int i = 0; i < CompilerConstants.NUM_INDEX_COLS; i++) {
            Instance value;
            if (i < fields.size())
                value = indexKeyRT.getField(fields.get(i));
            else
                value = Instances.nullInstance();
            bytes[i] = BytesUtils.toIndexBytes(value);
        }
        return new IndexKeyPO(indexKeyRT.getIndex().getId(), bytes);
    }

    private IndexKeyRT convertFromKey(IndexKeyPO key, IEntityContext context) {
        var index = context.getEntity(Index.class, key.getIndexId());
        var fields = index.getFields();
        var fieldMap = new HashMap<IndexField, Instance>();
        int i = 0;
        for (IndexField field : fields) {
            try (var input = new IndexKeyReader(new ByteArrayInputStream(key.getColumn(i)), context.getInstanceContext()::get)) {
                fieldMap.put(field, input.readInstance());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new IndexKeyRT(index, fieldMap);
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
    public List<Long> query(InstanceIndexQuery query, IInstanceContext context) {
        return index.query(convertQuery(query)).ids();
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
    public List<Long> scan(IndexKeyRT from, IndexKeyRT to, IInstanceContext context) {
        return index.scan(from.toPO(), to.toPO());
    }

    @Override
    public List<Long> queryByType(long typeId, long startId, long limit, IInstanceContext context) {
        return typeIndex.query(typeId, startId, limit);
    }

    public void setContextFactory(CompilerInstanceContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }
}
