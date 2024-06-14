package org.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.metavm.object.instance.ScanQuery;
import org.metavm.object.instance.core.TreeVersion;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.VersionPO;
import org.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

@Mapper
public interface InstanceMapper {

    InstancePO selectById(@Param("id") long id);

    List<InstancePO> selectByIds(@Param("appId") long appId, @Param("ids") Collection<Long> ids,
                                 @Param("lockMode") int lockMode);

//    List<InstancePO> selectByTypeIds(@Param("appId") long appId,
//                                     @Param("queries") Collection<ByTypeQuery> queries);

    void batchInsert(Collection<InstancePO> records);

    void batchUpdate(Collection<InstancePO> records);

    default void batchDelete1(List<InstancePO> toDeletes) {
        if(toDeletes.isEmpty())
            return;
        long appId = toDeletes.get(0).getAppId();
        long timestamp = System.currentTimeMillis();
        var versions = NncUtils.map(toDeletes, InstancePO::nextVersion);
        batchDelete(appId, timestamp, versions);
    }

    void batchDelete(@Param("appId") long appId,
                     @Param("timestamp") long timestamp,
                     @Param("versions") Collection<VersionPO> versions);

//    List<byte[]> getAliveIds(@Param("appId") long appId, @Param("ids") Collection<byte[]> ids);

    int updateSyncVersion(List<VersionPO> versions);

    List<InstancePO> scan(@Param("appId") long appId,
                          @Param("queries") Collection<ScanQuery> queries);

    List<Long> scanTrees(@Param("appId") long appId,
                         @Param("startId") long startId,
                         @Param("limit") long limit);


    List<TreeVersion> selectVersions(@Param("appId") long appId, @Param("ids") List<Long> ids);
}
