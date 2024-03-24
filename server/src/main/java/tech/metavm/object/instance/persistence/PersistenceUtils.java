package tech.metavm.object.instance.persistence;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.*;
import tech.metavm.system.RegionConstants;
import tech.metavm.util.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class PersistenceUtils {

    public static Set<IndexEntryPO> getIndexEntries(ClassInstance instance, ParameterizedFlowProvider parameterizedFlowProvider, long appId) {
        instance.ensureLoaded();
        return NncUtils.flatMapUnique(
                instance.getType().getConstraints(Index.class),
                c -> getIndexEntries(c, instance, parameterizedFlowProvider, appId)
        );
    }

    private static List<IndexEntryPO> getIndexEntries(Index index, ClassInstance instance, ParameterizedFlowProvider parameterizedFlowProvider, long appId) {
        List<IndexKeyRT> keys = index.createIndexKey(instance, parameterizedFlowProvider);
        return NncUtils.map(
                keys,
                key -> new IndexEntryPO(appId, key.toPO(), requireNonNull(instance.tryGetId()).toBytes())
        );
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
                classInstance.getId().getPhysicalId(),
                InstanceOutput.toByteArray(classInstance),
                classInstance.getVersion(),
                classInstance.getSyncVersion(),
                classInstance.getNextNodeId()
        );
    }

    private static InstancePO toInstancePO(ArrayInstance arrayInstance, long appId) {
        return new InstancePO(
                appId,
                arrayInstance.getId().getPhysicalId(),
                InstanceOutput.toByteArray(arrayInstance),
                arrayInstance.getVersion(),
                arrayInstance.getSyncVersion(),
                arrayInstance.getNextNodeId()
        );
    }

    public static boolean containsNull(Index index, IndexKeyPO key) {
        return NncUtils.anyMatch(index.getFields(), item -> isItemNull(item, key));
    }

    private static boolean isItemNull(IndexField field, IndexKeyPO key) {
        return Arrays.equals(key.getColumn(field.getIndex().getFieldIndex(field)), IndexKeyPO.NULL);
    }

    public static IndexQueryPO toIndexQueryPO(InstanceIndexQuery query, long appId, int lockMode) {
        return new IndexQueryPO(
                appId,
                query.index().getPhysicalId(),
                NncUtils.map(query.items(), item -> new IndexQueryItemPO(
                        "column" + item.field().getFieldIndex(),
                        item.operator(), BytesUtils.toIndexBytes(item.value()))),
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

    private static Set<ReferencePO> extractReferences(final ClassType classType, InstancePO instancePO) {
        var appId = instancePO.getAppId();
        Set<ReferencePO> refs = new HashSet<>();
        new StreamVisitor(new ByteArrayInputStream(instancePO.getData())) {

            private Id sourceId;
            private Id fieldId;

            @Override
            public void visitRecordBody(Id id) {
                var oldSourceId = sourceId;
                var oldFieldId = fieldId;
                sourceId = id;
                if (RegionConstants.isArrayId(id)) {
                    fieldId = null;
                    int len = readInt();
                    for (int i = 0; i < len; i++)
                        visit();
                } else {
                    int numFields = readInt();
                    for (int i = 0; i < numFields; i++) {
                        fieldId = readId();
                        visit();
                    }
                }
                sourceId = oldSourceId;
                fieldId = oldFieldId;
            }

            @Override
            public void visitReference() {
                var targetId = readId();
                refs.add(new ReferencePO(
                        appId,
                        sourceId.toBytes(),
                        targetId.toBytes(),
                        NncUtils.get(fieldId, Id::toBytes),
                        ReferenceKind.STRONG.code()
                ));
            }
        }.visit();
        return refs;
    }

}
