package org.metavm.object.instance.persistence.mappers;

import org.metavm.object.instance.persistence.IndexEntryPO;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.instance.persistence.IndexQueryPO;

import java.util.Collection;
import java.util.List;

public interface IndexEntryMapper {

    long count(IndexQueryPO queryPO);

    List<IndexEntryPO> query(IndexQueryPO queryPO);

    long countRange(long appId, IndexKeyPO from, IndexKeyPO to);

    List<IndexEntryPO> scan(long appId, IndexKeyPO from, IndexKeyPO to);

    List<IndexEntryPO> selectByInstanceIds(long appId, Collection<byte[]> instanceIds);

    List<IndexEntryPO> selectByKeys(long appId, Collection<IndexKeyPO> keys);

    void batchInsert(Collection<IndexEntryPO> items);

    void tryBatchInsert(Collection<IndexEntryPO> items);

    void batchDelete(Collection<IndexEntryPO> items);

}
