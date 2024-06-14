package org.metavm.entity;

import org.metavm.object.instance.InstanceStore;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.mappers.InstanceMapper;
import org.metavm.object.instance.persistence.mappers.*;
import org.metavm.util.NncUtils;

import java.util.List;

public class MemInstanceStore extends InstanceStore {

    private final IndexEntryMapper indexEntryMapper;

    public MemInstanceStore() {
        this(new MemIndexEntryMapper());
    }

    public MemInstanceStore(MemIndexEntryMapper indexEntryMapper) {
        this(
                new MemInstanceMapper(),
                indexEntryMapper,
                new MemReferenceMapper()
        );
    }

    public MemInstanceStore(InstanceMapper instanceMapper,
                            IndexEntryMapper indexEntryMapper,
                            ReferenceMapper referenceMapper) {
        super(instanceMapper,
                indexEntryMapper, referenceMapper);
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
