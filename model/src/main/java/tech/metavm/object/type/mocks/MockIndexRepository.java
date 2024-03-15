package tech.metavm.object.type.mocks;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Index;
import tech.metavm.object.type.IndexRepository;

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
