package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.*;

import java.util.List;
import java.util.function.Consumer;

@Wire(52)
@Entity
@Slf4j
public abstract class ScanTask extends Task {

    public static final int DEFAULT_BATCH_SIZE = 256;

    public static long BATCH_SIZE = DEFAULT_BATCH_SIZE;

    private long cursor;

    protected ScanTask(Id id, String title) {
        super(id, title);
    }

    @Override
    protected boolean run0(IInstanceContext context, IInstanceContext taskContext) {
        if(cursor == 0)
            onStart(context, taskContext);
        var r = scan(context, cursor, BATCH_SIZE);
        var batch = r.instances();
        process(batch, context, taskContext);
        context.validate();
        if (!r.completed()) {
            cursor = r.cursor();
            return false;
        } else
            return true;
    }

    @Override
    protected void onSuccess(IInstanceContext context, IInstanceContext taskContext) {
        onScanOver(context, taskContext);
    }

    @Override
    protected boolean run1(IInstanceContext context, IInstanceContext taskContext) {
        throw new UnsupportedOperationException();
    }

    protected void onStart(IInstanceContext context, IInstanceContext taskContext) {
    }

    protected void onScanOver(IInstanceContext context, IInstanceContext taskContext) {}

    protected ScanResult scan(IInstanceContext context,
                              long cursor,
                              @SuppressWarnings("SameParameterValue") long limit) {
        return context.scan(cursor, limit);
    }

    protected abstract void process(List<Instance> batch, IInstanceContext context, IInstanceContext taskContext);

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
