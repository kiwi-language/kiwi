package org.metavm.task;

import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.TreeNotFoundException;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceVisitor;

import java.util.List;

public class ReferenceRedirector extends ReferenceScanner {

    protected ReferenceRedirector(String id) {
        super("ReferenceRedirector-" + id, id);
    }

    @Override
    protected void process(List<Instance> referring) {
        var id = getTargetId();
        if(!referring.isEmpty()) {
            for (Instance root : referring) {
                root.accept(new InstanceVisitor() {
                    @Override
                    public void visitInstance(Instance instance) {
                        instance.transformReference((ref, isChild) -> {
                            if (id.equals(ref.tryGetId())) {
                                ref = ref.forward();
                            }
                            if (isChild)
                                ref.resolve().accept(this);
                            return ref;
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
            context.getInstanceContext().removeForwardingPointer(target, true);
        }
        catch (TreeNotFoundException ignored) {}
    }
}
