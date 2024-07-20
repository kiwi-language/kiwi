package org.metavm.task;

import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.TreeNotFoundException;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.DurableInstanceVisitor;
import org.metavm.object.instance.core.Id;

public class ForwardedFlagSetter extends Task {

    public static final int BATCH_SIZE = 256;

    private final String id;
    private long next;

    public ForwardedFlagSetter(String id) {
        super("MigrationMarkingTask-" + id);
        this.id = id;
    }

    @Override
    protected boolean run0(IEntityContext context) {
        var id = Id.parse(this.id);
        var referring = context.getInstanceContext().getByReferenceTargetId(id, next, BATCH_SIZE);
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
            next = referring.get(referring.size() - 1).getTreeId() + 1;
        }
        if(referring.size() < BATCH_SIZE) {
            onTaskDone(context, id);
            return true;
        }
        else
            return false;
    }

    private void onTaskDone(IEntityContext context, Id id) {
        try {
            var target = context.getInstanceContext().get(id);
            target.switchId();
            context.bind(new ReferenceRedirecter(this.id));
        }
        catch (TreeNotFoundException ignored) {
        }
    }

}
