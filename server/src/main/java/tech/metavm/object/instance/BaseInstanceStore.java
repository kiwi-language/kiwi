package tech.metavm.object.instance;

import tech.metavm.entity.StoreLoadRequest;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.persistence.InstancePO;

import java.util.List;

public abstract class BaseInstanceStore implements IInstanceStore {

    @Override
    public final List<InstancePO> load(StoreLoadRequest request, IInstanceContext context) {
        List<InstancePO> instancePOs = loadInternally(request, context);
//        clearStaleReferences(instancePOs, context);
        return instancePOs;
    }

    protected abstract List<InstancePO> loadInternally(StoreLoadRequest request, IInstanceContext context);


}
