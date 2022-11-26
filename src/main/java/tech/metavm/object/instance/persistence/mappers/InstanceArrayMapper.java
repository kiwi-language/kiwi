package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstanceArrayPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface InstanceArrayMapper {

    List<InstanceArrayPO> selectByTypeIds(@Param("tenantId") long tenantId,
                                          @Param("typeIds") Collection<Long> typeIds,
                                          @Param("start") long start,
                                          @Param("limit") long limit);

    List<InstanceArrayPO> selectByIds(long tenantId, Collection<Long> ids);

    void batchUpdate(Collection<InstanceArrayPO> records);

    void batchInsert(Collection<InstanceArrayPO> records);

    void batchDelete(Collection<Long> ids);

}
