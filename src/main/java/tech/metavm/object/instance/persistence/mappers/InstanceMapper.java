package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.InstanceTitlePO;
import tech.metavm.object.instance.persistence.VersionPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface InstanceMapper {

    List<InstancePO> selectByIds(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids);

    List<InstancePO> selectByTypeIds(@Param("tenantId") long tenantId,
                                     @Param("typeIds") Collection<Long> typeIds,
                                     @Param("startIdExclusive") long startIdExclusive,
                                     @Param("limit") long limit);

    void batchInsert(List<InstancePO> records);

    void batchUpdate(List<InstancePO> records);

    void batchDelete(@Param("tenantId") long tenantId,
                     @Param("timestamp") long timestamp,
                     @Param("versions") Collection<VersionPO> versions);

    int updateSyncVersion(List<VersionPO> versions);

    List<InstanceTitlePO> selectTitleByIds(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids);

}
