package org.metavm.object.type.mocks;

import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexRepository;

import java.util.HashMap;
import java.util.Map;

public class MockIndexRepository implements IndexRepository {

    private final Map<Id, Index> map = new HashMap<>();

    @Override
    public Index getIndex(Id id) {
        return map.get(id);
    }

    @Override
    public void save(Index index) {
        map.put(index.getId(), index);
    }

}
