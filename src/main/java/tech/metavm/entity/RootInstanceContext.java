package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;

import java.util.*;

public class RootInstanceContext implements IInstanceContext {

    private static volatile RootInstanceContext INSTANCE;

    public static RootInstanceContext getInstance() {
        if(INSTANCE == null) {
            synchronized (RootInstanceContext.class) {
                if(INSTANCE == null) {
                    INSTANCE = new RootInstanceContext();
                }
            }
        }
        return INSTANCE;
    }

    private final Map<Long, Instance> instanceMap = new HashMap<>();
    private final RootEntityContext entityContext;

    private RootInstanceContext() {
        this.entityContext = new RootEntityContext(this);
    }

    public void addInstance(Instance instance) {
        instanceMap.put(instance.getId(), instance);
    }

    @Override
    public List<Instance> batchGet(Collection<Long> ids, LoadingOption firstOption, LoadingOption... restOptions) {
        Set<LoadingOption> options =  new HashSet<>();
        options.add(firstOption);
        options.addAll(Arrays.asList(restOptions));
        return batchGet(ids, options);
    }

    @Override
    public List<Instance> batchGet(Collection<Long> ids, Set<LoadingOption> options) {
        return NncUtils.mapAndFilter(
                ids,
                instanceMap::get,
                Objects::nonNull
        );
    }

    @Override
    public RootEntityContext getEntityContext() {
        return entityContext;
    }

    @Override
    public boolean containsId(long id) {
        return instanceMap.containsKey(id);
    }

    @Override
    public void finish() {

    }
}
