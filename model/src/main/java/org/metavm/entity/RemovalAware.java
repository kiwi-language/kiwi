package org.metavm.entity;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;

import java.util.List;

public interface RemovalAware {

    List<Instance> beforeRemove(IInstanceContext context);

}
