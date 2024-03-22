package tech.metavm.object.instance.persistence;

import tech.metavm.entity.InstanceIndexQuery;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.ReferenceKind;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.type.*;
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
                classInstance.getId().toBytes(),
                classInstance.getType().getId().toBytes(),
                classInstance.getTitle(),
                InstanceOutput.toByteArray(classInstance),
                NncUtils.get(classInstance.getParent(), p -> p.getId().toBytes()),
                NncUtils.get(classInstance.getParentField(), f -> f.getId().toBytes()),
                classInstance.getRoot().getId().toBytes(),
                classInstance.getVersion(),
                classInstance.getSyncVersion()
        );
    }

    private static InstancePO toInstancePO(ArrayInstance arrayInstance, long appId) {
        return new InstancePO(
                appId,
                arrayInstance.getId().toBytes(),
                arrayInstance.getType().getId().toBytes(),
                arrayInstance.getTitle(),
                InstanceOutput.toByteArray(arrayInstance),
                NncUtils.get(arrayInstance.getParent(), p -> p.getId().toBytes()),
                NncUtils.get(arrayInstance.getParentField(), f -> f.getId().toBytes()),
                arrayInstance.getRoot().getId().toBytes(),
                arrayInstance.getVersion(),
                arrayInstance.getSyncVersion()
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
        Set<ReferencePO> refs = new HashSet<>();
        new StreamVisitor(new ByteArrayInputStream(instancePO.getData())) {
            @Override
            public void visitField() {
                var field = classType.getField(readId());
                var wireType = read();
                if (wireType == WireTypes.REFERENCE) {
                    refs.add(new ReferencePO(
                            instancePO.getAppId(),
                            instancePO.getId(),
                            readId().toBytes(),
                            field.getId().toBytes(),
                            ReferenceKind.getFromType(field.getType()).code()
                    ));
                } else
                    super.visit(wireType);
            }
        }.visit();
        return refs;
    }

    private static Set<ReferencePO> extractReferences(final ArrayType arrayType, InstancePO instancePO) {
        Set<ReferencePO> refs = new HashSet<>();
        new StreamVisitor(new ByteArrayInputStream(instancePO.getData())) {
            @Override
            public void visitReference() {
                var targetId = readId();
                refs.add(new ReferencePO(
                        instancePO.getAppId(),
                        instancePO.getId(),
                        targetId.toBytes(),
                        null,
                        ReferenceKind.getFromType(arrayType.getElementType()).code()
                ));
            }
        }.visit();
        return refs;
    }
}
