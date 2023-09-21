package tech.metavm.entity;

import tech.metavm.object.instance.ClassInstance;

public interface UpdateAware {

    void onUpdate(ClassInstance instance);

}
