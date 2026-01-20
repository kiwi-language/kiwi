package org.manul.task;

import org.manul.api.Entity;
import org.manul.object.instance.core.ClassInstance;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.wire.Wire;

import java.util.List;

@Wire(202)
@Entity
public class ReindexTask extends ScanTask {
    public ReindexTask(Id id, String title) {
        super(id, title);
    }

    @Override
    protected void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext) {
        for (Instance instance : batch) {
            if (instance instanceof ClassInstance o && !o.isValue())
                context.forceReindex(o);
        }
    }
}
