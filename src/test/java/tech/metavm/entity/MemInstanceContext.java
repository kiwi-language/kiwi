package tech.metavm.entity;

import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ChangeList;
import tech.metavm.util.InternalException;
import tech.metavm.util.MockIdProvider;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;
import static tech.metavm.util.TestConstants.TENANT_ID;

public class MemInstanceContext extends BaseInstanceContext {

    private boolean finished;
    private final Map<IndexKeyPO, List<Long>> index = new HashMap<>();
    private IEntityContext entityContext;
    private final IInstanceStore instanceStore;
    private @Nullable Function<Long, Type> typeProvider;

    public MemInstanceContext() {
        super(TENANT_ID, new MockIdProvider(), null);
        instanceStore = new MemInstanceStore();
    }

    public MemInstanceContext(long tenantId,
                              EntityIdProvider idProvider,
                              IInstanceStore instanceStore,
                              IInstanceContext parent) {
        super(tenantId, idProvider, parent);
        this.instanceStore = instanceStore;
    }

    public MemInstanceContext initData(Collection<Instance> instances) {
        replace(instances);
        return this;
    }

    public void setTypeProvider(@Nullable Function<Long, Type> typeProvider) {
        this.typeProvider = typeProvider;
    }

    void addIndex(IndexKeyPO key, long instanceId) {
        index.computeIfAbsent(key, k -> new ArrayList<>()).add(instanceId);
    }

    @Override
    protected Instance createInstance(long id) {
        throw new InternalException("Can not find instance for id " + id);
    }

    @Override
    public IEntityContext getEntityContext() {
        return entityContext;
    }

    @Override
    public void finish() {
        NncUtils.requireFalse(finished, "already finished");
        initIds();
        ChangeList<InstancePO> changeList = ChangeList.inserts(
                NncUtils.map(instances, instance -> instance.toPO(ROOT_TENANT_ID))
        );
        instanceStore.save(changeList);
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public Type getType(long id) {
        NncUtils.requireNonNull(typeProvider, "typeProvider not set");
        return typeProvider.apply(id);
    }

    @Override
    public List<Instance> selectByKey(IndexKeyPO indexKeyPO) {
        List<Long> instanceIds = index.get(indexKeyPO);
        return NncUtils.map(instanceIds, this::get);
    }

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }
}
