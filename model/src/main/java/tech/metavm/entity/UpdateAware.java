package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;

public interface UpdateAware {

    void onUpdate(ClassInstance instance);

}
