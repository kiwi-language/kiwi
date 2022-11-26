package tech.metavm.object.instance.persistence.mappers;

import org.springframework.stereotype.Component;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.InstanceTitlePO;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Component
public class InstanceMapperGateway {

    private final InstanceMapper instanceMapper;
    private final InstanceArrayMapper instanceArrayMapper;

    public InstanceMapperGateway(InstanceMapper instanceMapper, InstanceArrayMapper instanceArrayMapper) {
        this.instanceMapper = instanceMapper;
        this.instanceArrayMapper = instanceArrayMapper;
    }

    public List<InstancePO> selectByIds(long tenantId, Collection<Long> ids) {
        return NncUtils.splitAndMerge(
                ids,
                this::isArrayId,
                arrayIds -> new ArrayList<>(instanceArrayMapper.selectByIds(tenantId, arrayIds)),
                instanceIds -> instanceMapper.selectByIds(tenantId, ids)
        );
    }

    public List<InstancePO> selectByInstanceTypeIds(long tenantId, Collection<Long> typeIds, long start, long limit) {
        return instanceMapper.selectByTypeIds(tenantId, typeIds, start, limit);
    }

    public void batchInsert(List<InstancePO> deletes) {
        splitAndExecute(
                deletes,
                instanceMapper::batchInsert,
                instanceArrayMapper::batchInsert
        );
    }
    public void batchUpdate(List<InstancePO> deletes) {
        splitAndExecute(
                deletes,
                instanceMapper::batchUpdate,
                instanceArrayMapper::batchUpdate
        );
    }

    public void batchDelete(List<InstancePO> deletes) {
        if(NncUtils.isEmpty(deletes)) {
            return;
        }
        long tenantId = deletes.get(0).getTenantId();
        splitAndExecute(
                deletes,
                instances -> instanceMapper.batchDelete(tenantId, NncUtils.map(instances, InstancePO::nextVersion)),
                arrays -> instanceArrayMapper.batchDelete(NncUtils.mapToIds(arrays))
        );
    }

    private void splitAndExecute(List<InstancePO> records,
                                 Consumer<List<InstancePO>> instanceAction,
                                 Consumer<List<InstanceArrayPO>> arrayAction) {
        List<InstanceArrayPO> arrayPOs = NncUtils.filterAndMap(
                records,
                InstanceArrayPO.class::isInstance,
                InstanceArrayPO.class::cast
        );
        List<InstancePO> instancePOs = NncUtils.filterNot(
                records,
                InstanceArrayPO.class::isInstance
        );
        if(NncUtils.isNotEmpty(arrayPOs)) {
            arrayAction.accept(arrayPOs);
        }
        if(NncUtils.isNotEmpty(instancePOs)) {
            instanceAction.accept(instancePOs);
        }
    }

    private boolean isArrayId(long id) {
        return TypeCategory.ARRAY.idRangeContains(id);
    }


    public int updateSyncVersion(List<VersionPO> versions) {
        return instanceMapper.updateSyncVersion(versions);
    }

    public List<InstanceTitlePO> selectTitleByIds(long tenantId, List<Long> ids) {
        return instanceMapper.selectTitleByIds(tenantId, ids);
    }
}
