package org.manul.object.instance.core;

import org.manul.object.instance.IndexKeyRT;
import org.manul.object.instance.persistence.IndexKeyPO;
import org.manul.object.type.Index;
import org.manul.util.Utils;

import java.util.List;

public record InstanceIndexKey(Index index, List<Value> values) {

    public IndexKeyPO toPO() {
        return new IndexKeyPO(index.getId().toBytes(), IndexKeyRT.toKeyBytes(values));
    }

    public IndexKeyRT toRT() {
        return new IndexKeyRT(index, values);
    }

}
