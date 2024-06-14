package org.metavm.object.instance.core;

import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.persistence.IndexKeyPO;
import org.metavm.object.type.Index;
import org.metavm.util.BytesUtils;
import org.metavm.util.NncUtils;

import java.util.List;

public record InstanceIndexKey(Index index, List<Instance> values) {

    public IndexKeyPO toPO() {
        var columns = new byte[IndexKeyPO.MAX_KEY_COLUMNS][];
        for (int i = 0; i < IndexKeyPO.MAX_KEY_COLUMNS; i++) {
            if (i < values.size()) {
                columns[i] = BytesUtils.toIndexBytes(values.get(i));
            } else {
                columns[i] = IndexKeyPO.NULL;
            }
        }
        return new IndexKeyPO(index.getId().toBytes(), columns);
    }

    public IndexKeyRT toRT() {
        return new IndexKeyRT(index, NncUtils.zip(index.getFields(), values));
    }

}
