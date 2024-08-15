package org.metavm.entity;

import org.metavm.object.instance.InstanceStore;
import org.metavm.object.instance.cache.Cache;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.mappers.InstanceMapper;
import org.metavm.object.instance.persistence.mappers.*;
import org.metavm.util.NncUtils;

import java.util.List;

public class MemInstanceStore extends InstanceStore {

    private final IndexEntryMapper indexEntryMapper;

    public MemInstanceStore(Cache cache) {
        this(new MemIndexEntryMapper(), cache);
    }

    public MemInstanceStore(MemIndexEntryMapper indexEntryMapper, Cache cache) {
        this(
                new MemInstanceMapper(),
                indexEntryMapper,
                new MemReferenceMapper(),
                cache
        );
    }

    public MemInstanceStore(InstanceMapper instanceMapper,
                            IndexEntryMapper indexEntryMapper,
                            ReferenceMapper referenceMapper,
                            Cache cache
                            ) {
        super(instanceMapper,
                indexEntryMapper, referenceMapper, cache);
        this.indexEntryMapper = indexEntryMapper;
    }

    public MemIndexEntryMapper getIndexEntryMapper() {
        return (MemIndexEntryMapper) indexEntryMapper;
    }

    public InstancePO get(long appId, long id) {
        return NncUtils.first(instanceMapper.selectByIds(appId, List.of(id), 0));
    }

    public MemInstanceMapper getInstanceMapper() {
        return (MemInstanceMapper) instanceMapper;
    }

    public MemReferenceMapper getReferenceMapper() {
        return (MemReferenceMapper) referenceMapper;
    }

}
