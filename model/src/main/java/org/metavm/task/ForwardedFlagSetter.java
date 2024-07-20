package org.metavm.task;

import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.TreeNotFoundException;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.DurableInstanceVisitor;
import org.metavm.object.instance.core.Id;

import java.util.List;

public class ForwardedFlagSetter extends ReferenceScanner {

    public ForwardedFlagSetter(String id) {
        super("MigrationMarkingTask-" + id, id);
    }

    @Override
    protected void process(List<DurableInstance> referring) {
        var id = getTargetId();
        if (!referring.isEmpty()) {
            for (DurableInstance root : referring) {
                root.accept(new DurableInstanceVisitor() {
                    @Override
                    public void visitDurableInstance(DurableInstance instance) {
                        instance.forEachReference((ref, isChild) -> {
                            if (id.equals(ref.tryGetId()))
                                ref.setForwarded();
                            if (isChild)
                                ref.resolve().accept(this);
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void onTaskDone(IEntityContext context, Id id) {
        try {
            var target = context.getInstanceContext().get(id);
            target.switchId();
            context.bind(new ReferenceRedirector(id.toString()));
        }
        catch (TreeNotFoundException ignored) {
        }
    }

}
