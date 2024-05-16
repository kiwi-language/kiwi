package tech.metavm.object.instance.persistence;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;
import tech.metavm.system.RegionConstants;
import tech.metavm.util.*;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class PersistenceUtils {

    public static void forEachIndexEntries(ClassInstance instance, long appId, Consumer<IndexEntryPO> action,
                                           Consumer<IndexEntryPO> actionForUnique) {
        instance.ensureLoaded();
        instance.getKlass().getConstraints(Index.class).forEach(index -> forEachIndexEntries(index, instance, appId, action, actionForUnique));
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

    public static InstancePO toInstancePO(DurableInstance instance, long appId) {
        instance.ensureLoaded();
        return switch (instance) {
            case ClassInstance classInstance -> toInstancePO(classInstance, appId);
            case ArrayInstance arrayInstance -> toInstancePO(arrayInstance, appId);
            default -> throw new IllegalStateException("Unexpected value: " + instance);
        };
    }

    private static InstancePO toInstancePO(ClassInstance classInstance, long appId) {
        classInstance.ensureAllFieldsInitialized();
        return new InstancePO(
                appId,
                classInstance.getTreeId(),
                InstanceOutput.toByteArray(classInstance),
                classInstance.getVersion(),
                classInstance.getSyncVersion(),
                classInstance.getNextNodeId()
        );
    }

    private static InstancePO toInstancePO(ArrayInstance arrayInstance, long appId) {
        return new InstancePO(
                appId,
                arrayInstance.getTreeId(),
                InstanceOutput.toByteArray(arrayInstance),
                arrayInstance.getVersion(),
                arrayInstance.getSyncVersion(),
                arrayInstance.getNextNodeId()
        );
    }

    public static boolean containsNull(Index index, IndexKeyPO key) {
        for (int i = 0; i < index.getNumFields(); i++) {
           if(Arrays.equals(key.getColumn(i), IndexKeyPO.NULL))
               return true;
        }
        return false;
//        return NncUtils.anyMatch(index.getFields(), item -> isItemNull(item, key));
    }

    private static boolean isItemNull(IndexField field, IndexKeyPO key) {
        return Arrays.equals(key.getColumn(field.getIndex().getFieldIndex(field)), IndexKeyPO.NULL);
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

}
