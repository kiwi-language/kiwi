package org.metavm.mocks;

import lombok.Getter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.task.Task;

import java.util.function.Consumer;

@Getter
@Wire(99)
@Entity
public class TestTask extends Task {

    private int count = 0;

    public TestTask(Id id) {
        super(id, "Test Job");
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        return ++count >= 10;
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
