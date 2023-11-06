package tech.metavm.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.ClassType;

public abstract class ScanByClassTask extends ScanByTypeTask {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScanByClassTask.class);

    protected ScanByClassTask(String title, ClassType type) {
        super(title, type);
    }

    @Override
    protected final void processInstance(Instance instance, IInstanceContext context) {
        if (instance instanceof ClassInstance classInstance) {
            processClassInstance(classInstance);
        } else {
            LOGGER.error("Not a class instance: " + instance);
        }
    }

    protected abstract void processClassInstance(ClassInstance instance);
}
