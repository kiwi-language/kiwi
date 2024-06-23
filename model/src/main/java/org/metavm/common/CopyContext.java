package org.metavm.common;

import org.metavm.common.rest.dto.Copyable;

import java.util.HashMap;
import java.util.Map;

public interface CopyContext {

    static CopyContext create(Map<String, String> idMap) {
        return idMap.isEmpty() ? EmptyCopyContext.INSTANCE : new DefaultCopyContext(idMap);
    }

    String mapId(String id);

    <T> T copy(T object);

}

class EmptyCopyContext implements CopyContext {

    static EmptyCopyContext INSTANCE = new EmptyCopyContext();

    private EmptyCopyContext() {
    }

    @Override
    public String mapId(String id) {
        return id;
    }

    @Override
    public <T> T copy(T object) {
        return object;
    }
}

class DefaultCopyContext implements CopyContext {
    private final Map<String, String> idMap = new HashMap<>();

    DefaultCopyContext(Map<String, String> idMap) {
        this.idMap.putAll(idMap);
    }

    public String mapId(String id) {
        return idMap.getOrDefault(id, id);
    }

    public <T> T copy(T dto) {
        if(!idMap.isEmpty() && dto instanceof Copyable<?> copyable)
            //noinspection unchecked
            return (T) copyable.copy(this);
        else
            return dto;
    }

}


