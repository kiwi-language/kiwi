package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.ScanQuery;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.Version;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

@Mapper
public interface InstanceMapper {

    InstancePO selectById(@Param("id") byte[] id);

    List<InstancePO> selectByIds(@Param("appId") long appId, @Param("ids") Collection<byte[]> ids,
                                 @Param("lockMode") int lockMode);

    List<InstancePO> selectByTypeIds(@Param("appId") long appId,
                                     @Param("queries") Collection<ByTypeQuery> queries);

    List<InstancePO> selectForest(@Param("appId") long appId, @Param("ids") Collection<byte[]> ids,
                                  @Param("lockMode") int lockMode);

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

    List<byte[]> getAliveIds(@Param("appId") long appId, @Param("ids") Collection<byte[]> ids);

    int updateSyncVersion(List<VersionPO> versions);

    List<InstancePO> scan(@Param("appId") long appId,
                          @Param("queries") Collection<ScanQuery> queries);

    List<Long> selectVersions(@Param("ids") List<byte[]> ids);

    List<Version> selectRootVersions(@Param("appId") long appId, @Param("ids") List<byte[]> ids);
}
