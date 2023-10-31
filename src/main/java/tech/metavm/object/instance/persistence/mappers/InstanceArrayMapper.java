package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.ByTypeQuery;
import tech.metavm.object.instance.persistence.InstanceArrayPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface InstanceArrayMapper {

    List<InstanceArrayPO> selectByTypeIds(@Param("tenantId") long tenantId,
                                          @Param("queries") Collection<ByTypeQuery> queries);

    List<InstanceArrayPO> selectByIds(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids,
                                      @Param("lockMode") int lockMode);

    void batchUpdate(Collection<InstanceArrayPO> records);

    void batchInsert(Collection<InstanceArrayPO> records);

    void batchDelete(Collection<Long> ids);

    List<InstanceArrayPO> head();

    List<Long> getAliveIds(@Param("tenantId") long tenantId, @Param("ids") Collection<Long> ids);
}
