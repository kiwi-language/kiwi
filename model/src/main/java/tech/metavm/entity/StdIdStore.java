package tech.metavm.entity;

import java.util.Map;

public interface StdIdStore {

    void save(Map<String, Long> ids);

    Map<String, Long> load();

}
