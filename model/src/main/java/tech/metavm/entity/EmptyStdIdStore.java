package tech.metavm.entity;

import java.util.Map;

public class EmptyStdIdStore implements StdIdStore{

    @Override
    public void save(Map<String, Long> ids) {

    }

    @Override
    public Map<String, Long> load() {
        return Map.of();
    }
}
