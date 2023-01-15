package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.IndexQueryPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface IndexEntryMapper {

    Long countByKeys(@Param("tenantId") long tenantId,
                     @Param("keys") List<IndexKeyPO> key);

    List<IndexEntryPO> selectByKeys(@Param("tenantId") long tenantId,
                                    @Param("keys") List<IndexKeyPO> keys);

    List<IndexEntryPO> query(IndexQueryPO queryPO);

    List<IndexEntryPO> selectByInstanceIdsOrKeys(
            @Param("tenantId") long tenantId,
            @Param("instanceIds") List<Long> instanceIds,
            @Param("keys") List<IndexKeyPO> keys
            );

    void batchInsert(Collection<IndexEntryPO> items);

    void batchDelete(Collection<IndexEntryPO> items);

}
