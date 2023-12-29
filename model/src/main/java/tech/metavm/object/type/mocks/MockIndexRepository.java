package tech.metavm.object.type.mocks;

import tech.metavm.common.RefDTO;
import tech.metavm.object.type.Index;
import tech.metavm.object.type.IndexRepository;

import java.util.HashMap;
import java.util.Map;

public class MockIndexRepository implements IndexRepository {

    private final Map<RefDTO, Index> map = new HashMap<>();

    @Override
    public Index getIndex(RefDTO ref) {
        return map.get(ref);
    }

    @Override
    public void save(Index index) {
        map.put(index.getRef(), index);
    }

}
