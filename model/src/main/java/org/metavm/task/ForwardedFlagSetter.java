package org.metavm.task;

import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.TreeNotFoundException;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.DurableInstanceVisitor;
import org.metavm.object.instance.core.Id;
import org.metavm.util.Constants;

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
            var redirector = new ReferenceRedirector(id.toString());
            if(Constants.SESSION_TIMEOUT != -1)
                redirector.setStartAt(System.currentTimeMillis() + (Constants.SESSION_TIMEOUT << 1));
            context.bind(redirector);
        }
        catch (TreeNotFoundException ignored) {
        }
    }

}
