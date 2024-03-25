package tech.metavm.object.instance.core;

import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.type.Index;
import tech.metavm.util.BytesUtils;
import tech.metavm.util.NncUtils;

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
