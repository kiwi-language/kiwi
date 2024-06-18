package org.metavm.task;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.ClassType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ScanByClassTask extends ScanByTypeTask {

    public static final Logger logger = LoggerFactory.getLogger(ScanByClassTask.class);

    protected ScanByClassTask(String title, ClassType type) {
        super(title, type);
    }

    @Override
    protected final void processInstance(Instance instance, IInstanceContext context) {
        if (instance instanceof ClassInstance classInstance) {
            processClassInstance(classInstance);
        } else {
            logger.error("Not a class instance: " + instance);
        }
    }

    protected abstract void processClassInstance(ClassInstance instance);
}
