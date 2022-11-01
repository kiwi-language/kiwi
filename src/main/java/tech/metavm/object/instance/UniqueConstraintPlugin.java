package tech.metavm.object.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.mappers.IndexItemMapper;
import tech.metavm.util.BusinessException;
import tech.metavm.util.ChangeList;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class UniqueConstraintPlugin implements ContextPlugin {

    private static final String ATTR_OLD_INDEX_ITEMS = "oldIndexItems";

    private static final String ATTR_CUR_INDEX_ITEMS = "curIndexItems";

    @Autowired
    private IndexItemMapper indexItemMapper;

    @Override
    public void beforeSaving(ContextDifference diff) {
        List<Instance> currentInstances = NncUtils.merge(diff.inserts(), diff.updates());
        Map<Long, Instance> instanceMap = NncUtils.toMap(currentInstances, AbsInstance::getId);
        List<IndexItemPO> currentItems = NncUtils.flatMap(currentInstances, Instance::getUniqueKeys);
        List<Instance> oldInstances = NncUtils.merge(diff.updates(), diff.deletes());
        List<IndexItemPO> oldItems = NncUtils.isEmpty(oldInstances) && NncUtils.isEmpty(currentItems) ? List.of() :
                indexItemMapper.selectByInstanceIdsOrKeys(
                        diff.tenantId(),
                        NncUtils.map(oldInstances, AbsInstance::getId),
                        NncUtils.map(currentItems, IndexItemPO::getKey)
                );

        Map<IndexKeyPO, Long> oldKeyMap = NncUtils.toMap(oldItems, IndexItemPO::getKey, IndexItemPO::getInstanceId);
        for (IndexItemPO currentItem : currentItems) {
            Long existingInstanceId = oldKeyMap.get(currentItem.getKey());
            if(existingInstanceId != null && !existingInstanceId.equals(currentItem.getInstanceId())) {
                throw BusinessException.duplicateKey(
                        instanceMap.get(currentItem.getInstanceId()), currentItem.getConstraintId()
                );
            }
        }
        diff.setAttribute(ATTR_CUR_INDEX_ITEMS, currentItems);
        diff.setAttribute(ATTR_OLD_INDEX_ITEMS, oldItems);
    }

    @Override
    public void afterSaving(ContextDifference difference) {
        List<IndexItemPO> oldItems = (List<IndexItemPO>) difference.getAttribute(ATTR_OLD_INDEX_ITEMS);
        List<IndexItemPO> currentItems = (List<IndexItemPO>) difference.getAttribute(ATTR_CUR_INDEX_ITEMS);
        ChangeList<IndexItemPO> changeList = ChangeList.build(oldItems, currentItems, Function.identity());
        if(NncUtils.isNotEmpty(changeList.inserts())) {
            indexItemMapper.batchInsert(changeList.inserts());
        }
        if(NncUtils.isNotEmpty(changeList.deletes())) {
            indexItemMapper.batchDelete(changeList.deletes());
        }
    }

}
