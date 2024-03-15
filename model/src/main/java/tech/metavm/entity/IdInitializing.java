package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;

public interface IdInitializing extends Identifiable {

    void initId(Id id);

    void clearId();

}
