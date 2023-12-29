package tech.metavm.object.view.mocks;

import tech.metavm.common.RefDTO;
import tech.metavm.object.view.Mapping;
import tech.metavm.object.view.MappingRepository;

import java.util.HashMap;
import java.util.Map;

public class MockMappingRepository implements MappingRepository {

    private final Map<RefDTO, Mapping> map = new HashMap<>();

    @Override
    public Mapping getMapping(RefDTO ref) {
        return map.get(ref);
    }

    @Override
    public void save(Mapping mapping) {
        map.put(mapping.getRef(), mapping);
    }
}
