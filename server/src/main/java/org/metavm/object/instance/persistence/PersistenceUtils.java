package org.metavm.object.instance.persistence;

import org.metavm.entity.InstanceIndexQuery;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.InstanceIndexKey;
import org.metavm.object.type.*;
import org.metavm.util.NncUtils;
import org.metavm.util.StreamVisitor;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class PersistenceUtils {

    // Used for debug. DO NOT REMOVE!!!
    @SuppressWarnings("unused")
    public static List<IndexEntryPO> getIndexEntries(ClassInstance instance, long appId) {
        var result = new ArrayList<IndexEntryPO>();
        forEachIndexEntries(instance, appId, result::add, e -> {});
        return result;
    }

    // Used for debug. DO NOT REMOVE!!!
    @SuppressWarnings("unused")
    public static List<IndexEntryPO> getIndexEntries(Index index, ClassInstance instance, long appId) {
        var result = new ArrayList<IndexEntryPO>();
        forEachIndexEntries(index, instance, appId, result::add, e -> {});
        return result;
    }

    public static void forEachIndexEntries(ClassInstance instance, long appId, Consumer<IndexEntryPO> action,
                                           Consumer<IndexEntryPO> actionForUnique) {
        instance.ensureLoaded();
        instance.getKlass().getAllConstraints(Index.class).forEach(index -> forEachIndexEntries(index, instance, appId, action, actionForUnique));
    }

    private static void forEachIndexEntries(Index index, ClassInstance instance,
                                            long appId, Consumer<IndexEntryPO> action, Consumer<IndexEntryPO> actionForUnique) {
        index.forEachIndexKey(instance,
                key -> {
                    var entryPO = new IndexEntryPO(appId, key.toPO(), requireNonNull(instance.tryGetId()).toBytes());
                    action.accept(entryPO);
                    if(key.getIndex().isUnique() && !containsNull(key.getIndex(), entryPO.getKey()))
                        actionForUnique.accept(entryPO);
                });
    }

    public static boolean containsNull(Index index, IndexKeyPO key) {
        var columns = key.getColumns();
        for (int i = 0; i < index.getNumFields(); i++) {
           if(Arrays.equals(columns.get(i), IndexKeyPO.NULL))
               return true;
        }
        return false;
//        return NncUtils.anyMatch(index.getFields(), item -> isItemNull(item, key));
    }

    private static boolean isItemNull(IndexField field, IndexKeyPO key) {
        return Arrays.equals(key.getColumns().get(field.getIndex().getFieldIndex(field)), IndexKeyPO.NULL);
    }

    public static IndexQueryPO toIndexQueryPO(InstanceIndexQuery query, long appId, int lockMode) {
        return new IndexQueryPO(
                appId,
                query.index().getId().toBytes(),
                NncUtils.get(query.from(), InstanceIndexKey::toPO),
                NncUtils.get(query.to(), InstanceIndexKey::toPO),
                query.desc(),
                query.limit(),
                lockMode
        );
    }

    public static Set<ReferencePO> extractReferences(Type type, InstancePO instancePO) {
        return switch (type) {
            case ClassType classType -> extractReferences(classType, instancePO);
            case ArrayType arrayType -> extractReferences(arrayType, instancePO);
            default -> Set.of();
        };
    }

    public static InstancePO buildInstancePO(long appId, long id, byte[] treeBytes) {
        var ref = new Object() {
            long version;
            long syncVersion;
            long nextNodeId;
        };
        new StreamVisitor(new ByteArrayInputStream(treeBytes)) {
            @Override
            public void visitVersion(long version) {
                ref.version = version;
            }

            @Override
            public void visitNextNodeId(long nextNodeId) {
                ref.nextNodeId = nextNodeId;
            }
        }.visitGrove();
        return new InstancePO(
                appId,
                id,
                treeBytes,
                ref.version,
                ref.syncVersion,
                ref.nextNodeId
        );
    }

}
