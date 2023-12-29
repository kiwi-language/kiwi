package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;

public interface ChangeAware {

    void onChange(ClassInstance instance, IEntityContext context);

}
