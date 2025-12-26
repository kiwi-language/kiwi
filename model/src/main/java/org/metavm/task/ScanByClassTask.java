package org.metavm.task;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;

import java.util.function.Consumer;

@Entity
@Wire(29)
@Slf4j
public abstract class ScanByClassTask extends ScanByTypeTask {

    protected ScanByClassTask(Id id, String title, ClassType type) {
        super(id, title, type);
    }

    @Override
    protected final void processInstance(Value instance, IInstanceContext context) {
        if (instance.isObject()) {
            processClassInstance(instance.resolveObject(), context);
        } else {
            log.error("Not a class instance: {}", instance);
        }
    }

    protected abstract void processClassInstance(ClassInstance instance, IInstanceContext context);

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
