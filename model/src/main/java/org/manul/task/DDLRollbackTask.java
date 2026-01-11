package org.manul.task;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

import java.util.function.Consumer;

@Entity
@Wire(37)
public class DDLRollbackTask extends Task {

    protected DDLRollbackTask(Id id) {
        super(id, "DDLRollbackTask");
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        return true;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
