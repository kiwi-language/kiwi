package tech.metavm.object.view.mocks;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.view.Mapping;
import tech.metavm.object.view.MappingRepository;

import java.util.HashMap;
import java.util.Map;

public class MockMappingRepository implements MappingRepository {

    private final Map<Id, Mapping> map = new HashMap<>();

    @Override
    public Mapping getMapping(Id id) {
        return map.get(id);
    }

    @Override
    public void save(Mapping mapping) {
        map.put(mapping.getId(), mapping);
    }
}
