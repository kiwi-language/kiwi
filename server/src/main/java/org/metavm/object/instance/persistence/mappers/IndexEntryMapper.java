package org.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.IndexQueryPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface IndexEntryMapper {

    long count(IndexQueryPO queryPO);

    List<IndexEntryPO> query(IndexQueryPO queryPO);

    long countRange(
            @Param("appId") long appId,
            @Param("from") IndexKeyPO from,
            @Param("to") IndexKeyPO to);

    List<IndexEntryPO> scan(
            @Param("appId") long appId,
            @Param("from") IndexKeyPO from,
            @Param("to") IndexKeyPO to);

    List<IndexEntryPO> selectByInstanceIdsOrKeys(
            @Param("appId") long appId,
            @Param("instanceIds") Collection<byte[]> instanceIds,
            @Param("keys") Collection<IndexKeyPO> keys
            );

    List<IndexEntryPO> selectByInstanceIds(
            @Param("appId") long appId,
            @Param("instanceIds") Collection<byte[]> instanceIds
    );

    List<IndexEntryPO> selectByKeys(@Param("appId") long appId, @Param("keys") Collection<IndexKeyPO> keys);

    void batchInsert(Collection<IndexEntryPO> items);

    void batchDelete(Collection<IndexEntryPO> items);

}
