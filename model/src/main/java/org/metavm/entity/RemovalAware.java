package org.metavm.entity;

import org.metavm.object.instance.core.Instance;

import java.util.List;

public interface RemovalAware {

    List<Instance> beforeRemove(IEntityContext context);

}
