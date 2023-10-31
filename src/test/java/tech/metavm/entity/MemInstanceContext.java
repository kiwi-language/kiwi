package tech.metavm.entity;

import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;
import static tech.metavm.util.TestConstants.TENANT_ID;

public class MemInstanceContext extends BaseInstanceContext {

    private boolean finished;
    private IEntityContext entityContext;
    private @Nullable Function<Long, Type> typeProvider;

    public MemInstanceContext() {
        this(TENANT_ID, new MockIdProvider(), new MemInstanceStore(), null);
    }

    public MemInstanceContext(long tenantId,
                              EntityIdProvider idProvider,
                              IInstanceStore instanceStore,
                              IInstanceContext parent) {
        super(tenantId, idProvider, instanceStore, MockRegistry.getDefContext(), parent);
        typeProvider = typeId -> getEntityContext().getType(typeId);
        setCreateJob(job -> getEntityContext().bind(job));
    }

    public MemInstanceContext initData(Collection<Instance> instances) {
        replace(instances);
        return this;
    }

    public void setTypeProvider(@Nullable Function<Long, Type> typeProvider) {
        this.typeProvider = typeProvider;
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
    protected void finishInternal() {
        NncUtils.requireFalse(finished, "already finished");
        initIds();
        ChangeList<InstancePO> changeList = ChangeList.inserts(
                NncUtils.map(instances, instance -> instance.toPO(ROOT_TENANT_ID))
        );
        instanceStore.save(changeList);
        finished = true;
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
    protected boolean checkAliveInStore(long id) {
        return instanceStore.load(
                StoreLoadRequest.create(id),
                this
        ).size() > 0;
    }

    @Override
    public IInstanceContext newContext(long tenantId) {
        return new MemInstanceContext(
                tenantId, idService, instanceStore, getParent()
        );
    }

    public void setEntityContext(IEntityContext entityContext) {
        this.entityContext = entityContext;
    }
}
