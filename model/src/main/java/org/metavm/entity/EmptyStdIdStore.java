package org.metavm.entity;

import org.metavm.object.instance.core.Id;

import java.util.Map;

public class EmptyStdIdStore implements StdIdStore{

    @Override
    public void save(Map<String, Id> ids) {

    }

    @Override
    public Map<String, Id> load() {
        return Map.of();
    }
}
