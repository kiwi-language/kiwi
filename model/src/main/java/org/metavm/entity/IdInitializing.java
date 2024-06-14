package org.metavm.entity;

import org.metavm.object.instance.core.Id;

public interface IdInitializing extends Identifiable {

    void initId(Id id);

    void clearId();

}
