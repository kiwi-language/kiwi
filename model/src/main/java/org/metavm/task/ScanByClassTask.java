package org.metavm.task;

import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ScanByClassTask extends ScanByTypeTask {

    public static final Logger logger = LoggerFactory.getLogger(ScanByClassTask.class);

    protected ScanByClassTask(String title, ClassType type) {
        super(title, type);
    }

    @Override
    protected final void processInstance(Value instance, IEntityContext context) {
        if (instance.isObject()) {
            processClassInstance(instance.resolveObject(), context);
        } else {
            logger.error("Not a class instance: " + instance);
        }
    }

    protected abstract void processClassInstance(ClassInstance instance, IEntityContext context);
}
