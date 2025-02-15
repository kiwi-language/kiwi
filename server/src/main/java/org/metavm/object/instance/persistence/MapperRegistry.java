package org.metavm.object.instance.persistence;

import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.instance.persistence.mappers.InstanceMapper;

public interface MapperRegistry {

    InstanceMapper getInstanceMapper(long appId, String table);

    IndexEntryMapper getIndexEntryMapper(long appId, String table);

}
