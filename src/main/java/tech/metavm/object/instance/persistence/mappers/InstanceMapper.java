package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.ScanQuery;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.InstanceTitlePO;
import tech.metavm.object.instance.persistence.VersionPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface InstanceMapper {

    List<InstancePO> selectByIds(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids,
                                 @Param("lockMode") int lockMode);

    List<InstancePO> selectByTypeIds(@Param("tenantId") long tenantId,
                                     @Param("queries") Collection<ByTypeQuery> queries);

    void batchInsert(Collection<InstancePO> records);

    void batchUpdate(Collection<InstancePO> records);

    void batchDelete(@Param("tenantId") long tenantId,
                     @Param("timestamp") long timestamp,
                     @Param("versions") Collection<VersionPO> versions);

    List<Long> getAliveIds(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids);

    int updateSyncVersion(List<VersionPO> versions);

    List<InstanceTitlePO> selectTitleByIds(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids);

    List<InstancePO> scan(@Param("tenantId") long tenantId,
                          @Param("queries") Collection<ScanQuery> queries);
}
