package org.metavm.object.instance.core;

import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.type.Index;
import org.metavm.util.Utils;

import java.util.List;

public record InstanceIndexKey(Index index, List<Value> values) {

    public IndexKeyPO toPO() {
        return new IndexKeyPO(index.getId().toBytes(), IndexKeyRT.toKeyBytes(values));
    }

    public IndexKeyRT toRT() {
        return new IndexKeyRT(index, Utils.zip(index.getFields(), values));
    }

}
