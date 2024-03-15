package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;

import java.util.Map;

public interface StdIdStore {

    void save(Map<String, Id> ids);

    Map<String, Id> load();

}
