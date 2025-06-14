package org.metavm.entity;

import org.metavm.object.instance.InstanceStore;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.instance.persistence.MapperRegistry;
import org.metavm.util.Utils;

import java.util.List;

public class MemInstanceStore extends InstanceStore {

    public MemInstanceStore(MapperRegistry mapperRegistry) {
        super(mapperRegistry);
    }

    public InstancePO get(long appId, long id) {
        return Utils.first(this.getInstanceMapper(appId).selectByIds(appId, List.of(id)));
    }

}
