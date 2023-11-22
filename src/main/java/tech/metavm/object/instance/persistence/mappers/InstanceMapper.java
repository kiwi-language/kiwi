package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.ScanQuery;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.InstanceTitlePO;
import tech.metavm.object.instance.persistence.Version;
import tech.metavm.object.instance.persistence.VersionPO;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

@Mapper
public interface InstanceMapper {

    InstancePO selectById(@Param("id") long id);

    List<InstancePO> selectByIds(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids,
                                 @Param("lockMode") int lockMode);

    List<InstancePO> selectByTypeIds(@Param("tenantId") long tenantId,
                                     @Param("queries") Collection<ByTypeQuery> queries);

    List<InstancePO> selectForest(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids,
                                  @Param("lockMode") int lockMode);

    void batchInsert(Collection<InstancePO> records);

    void batchUpdate(Collection<InstancePO> records);

    default void batchDelete1(List<InstancePO> toDeletes) {
        if(toDeletes.isEmpty())
            return;
        long tenantId = toDeletes.get(0).getTenantId();
        long timestamp = System.currentTimeMillis();
        var versions = NncUtils.map(toDeletes, InstancePO::nextVersion);
        batchDelete(tenantId, timestamp, versions);
    }

    void batchDelete(@Param("tenantId") long tenantId,
                     @Param("timestamp") long timestamp,
                     @Param("versions") Collection<VersionPO> versions);

    List<Long> getAliveIds(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids);

    int updateSyncVersion(List<VersionPO> versions);

    List<InstancePO> scan(@Param("tenantId") long tenantId,
                          @Param("queries") Collection<ScanQuery> queries);

    List<Long> selectVersions(@Param("ids") List<Long> ids);

    List<Version> selectRootVersions(@Param("tenantId") long tenantId, @Param("ids") List<Long> ids);
}
