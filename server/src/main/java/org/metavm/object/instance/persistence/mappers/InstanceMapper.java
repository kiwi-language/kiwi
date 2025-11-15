package org.metavm.object.instance.persistence.mappers;

import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.VersionPO;
import org.metavm.util.Utils;

import java.util.Collection;
import java.util.List;

public interface InstanceMapper {

    InstancePO selectById(long id);

    List<InstancePO> selectByIds(long appId, Collection<Long> ids);

    List<Long> filterDeletedIds(Collection<Long> ids);

    void batchInsert(Collection<InstancePO> records);

    void batchUpdate(Collection<InstancePO> records);

    void batchUpsert(Collection<InstancePO> records);

    default void batchDelete1(List<InstancePO> toDeletes) {
        if(toDeletes.isEmpty())
            return;
        long appId = toDeletes.getFirst().getAppId();
        long timestamp = System.currentTimeMillis();
        var versions = Utils.map(toDeletes, InstancePO::nextVersion);
        batchDelete(appId, timestamp, versions);
    }

    void batchDelete(long appId,
                     long timestamp,
                     Collection<VersionPO> versions);

    void physicalDelete(long appId, long id);

    void tryBatchDelete(long appId,
                     long timestamp,
                     Collection<VersionPO> versions);

    int updateSyncVersion(List<VersionPO> versions);

    List<Long> scanTrees(long appId,
                         long startId,
                         long limit);


    List<TreeVersion> selectVersions(long appId, List<Long> ids);
}
