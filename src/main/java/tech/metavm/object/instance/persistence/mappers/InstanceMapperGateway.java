//package tech.metavm.object.instance.persistence.mappers;
//
//import org.springframework.stereotype.Component;
//import tech.metavm.object.instance.ByTypeQuery;
//import tech.metavm.object.instance.ScanQuery;
//import tech.metavm.object.instance.persistence.InstancePO;
//import tech.metavm.object.instance.persistence.InstanceTitlePO;
//import tech.metavm.object.instance.persistence.VersionPO;
//import tech.metavm.object.type.TypeCategory;
//import tech.metavm.util.InternalException;
//import tech.metavm.util.NncUtils;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.function.Consumer;
//
//@Component
//public class InstanceMapperGateway {
//
//    private final InstanceMapper instanceMapper;
//    private final InstanceArrayMapper instanceArrayMapper;
//
//    public InstanceMapperGateway(InstanceMapper instanceMapper, InstanceArrayMapper instanceArrayMapper) {
//        this.instanceMapper = instanceMapper;
//        this.instanceArrayMapper = instanceArrayMapper;
//    }
//
//    public List<InstancePO> selectByIds(long tenantId, Collection<Long> ids, int lockMode) {
//        return NncUtils.splitAndMerge(
//                ids,
//                TypeCategory::isArrayId,
//                arrayIds -> new ArrayList<>(instanceArrayMapper.selectByIds(tenantId, arrayIds, lockMode)),
//                instanceIds -> instanceMapper.selectByIds(tenantId, instanceIds, lockMode)
//        );
//    }
//
//    public List<InstancePO> selectByTypeIds(long tenantId, Collection<ByTypeQuery> queries) {
//        return NncUtils.union(
//                instanceMapper.selectByTypeIds(tenantId, queries),
//                instanceArrayMapper.selectByTypeIds(tenantId, queries)
//        );
//    }
//
//    public List<InstancePO> scanInstances(long tenantId, List<ScanQuery> queries) {
//        return instanceMapper.scan(tenantId, queries);
//    }
//
//    public void batchInsert(List<InstancePO> inserts) {
//        try {
//            splitAndExecute(
//                    inserts,
//                    instanceMapper::batchInsert,
//                    instanceArrayMapper::batchInsert
//            );
//        } catch (Throwable e) {
//            throw new InternalException(e);
//        }
//    }
//
//    public void batchUpdate(List<InstancePO> updates) {
//        splitAndExecute(
//                updates,
//                instanceMapper::batchUpdate,
//                instanceArrayMapper::batchUpdate
//        );
//    }
//
//    public void batchDelete(List<InstancePO> deletes) {
//        if (NncUtils.isEmpty(deletes)) {
//            return;
//        }
//        long tenantId = deletes.get(0).getTenantId();
//        long timestamp = System.currentTimeMillis();
//        splitAndExecute(
//                deletes,
//                instances -> instanceMapper.batchDelete(tenantId, timestamp, NncUtils.map(instances, InstancePO::nextVersion)),
//                arrays -> instanceArrayMapper.batchDelete(NncUtils.mapToIds(arrays))
//        );
//    }
//
//    public List<Long> getAliveIds(long tenantId, Collection<Long> ids) {
//        return NncUtils.splitAndMerge(
//                ids,
//                TypeCategory::isArrayId,
//                arrayIds -> instanceArrayMapper.getAliveIds(tenantId, arrayIds),
//                objectIds -> instanceMapper.getAliveIds(tenantId, objectIds)
//        );
//    }
//
//    private void splitAndExecute(List<InstancePO> records,
//                                 Consumer<List<InstancePO>> instanceAction,
//                                 Consumer<List<InstanceArrayPO>> arrayAction) {
//        List<InstanceArrayPO> arrayPOs = NncUtils.filterAndMap(
//                records,
//                InstanceArrayPO.class::isInstance,
//                InstanceArrayPO.class::cast
//        );
//        List<InstancePO> instancePOs = NncUtils.removed(
//                records,
//                InstanceArrayPO.class::isInstance
//        );
//        if (NncUtils.isNotEmpty(arrayPOs)) {
//            arrayAction.accept(arrayPOs);
//        }
//        if (NncUtils.isNotEmpty(instancePOs)) {
//            instanceAction.accept(instancePOs);
//        }
//    }
//
//    public int updateSyncVersion(List<VersionPO> versions) {
//        List<VersionPO> objectVersions = NncUtils.removed(versions, v -> TypeCategory.isArrayId(v.id()));
//        if (NncUtils.isNotEmpty(objectVersions)) {
//            return instanceMapper.updateSyncVersion(objectVersions);
//        }
//        return 0;
//    }
//
//    public List<InstanceTitlePO> selectTitleByIds(long tenantId, List<Long> ids) {
//        return instanceMapper.selectTitleByIds(tenantId, ids);
//    }
//}
