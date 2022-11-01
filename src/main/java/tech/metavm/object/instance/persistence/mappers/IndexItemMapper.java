package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface IndexItemMapper {

    Long countByKeys(@Param("tenantId") long tenantId,
                     @Param("keys") List<IndexKeyPO> key);

    List<IndexItemPO> selectByKeys(@Param("tenantId") long tenantId,
                                   @Param("keys") List<IndexKeyPO> keys);

    List<IndexItemPO> selectByInstanceIdsOrKeys(
            @Param("tenantId") long tenantId,
            @Param("instanceIds") List<Long> instanceIds,
            @Param("keys") List<IndexKeyPO> keys
            );

    void batchInsert(Collection<IndexItemPO> items);

    void batchDelete(Collection<IndexItemPO> items);

}
