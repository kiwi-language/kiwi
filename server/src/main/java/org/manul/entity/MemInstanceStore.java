package org.manul.entity;

import org.manul.object.instance.InstanceStore;
import org.manul.object.instance.persistence.InstancePO;
import org.manul.object.instance.persistence.MapperRegistry;
import org.manul.util.Utils;

import java.util.List;

public class MemInstanceStore extends InstanceStore {

    public MemInstanceStore(MapperRegistry mapperRegistry) {
        super(mapperRegistry);
    }

    public InstancePO get(long appId, long id) {
        return Utils.first(this.getInstanceMapper(appId).selectByIds(appId, List.of(id)));
    }

}
