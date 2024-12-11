package org.metavm.task;

import org.metavm.api.Entity;
import org.metavm.object.instance.core.Instance;
import org.metavm.util.Instances;

import java.util.List;

@Entity
public class EagerFlagSetter extends ReferenceScanner {

    public EagerFlagSetter(String id) {
        super("EagerFlagSetter-" + id, id);
    }

    @Override
    protected void process(List<Instance> batch) {
        Instances.setEagerFlag(batch, getTargetId());
    }

}
