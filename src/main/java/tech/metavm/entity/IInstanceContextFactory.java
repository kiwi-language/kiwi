package tech.metavm.entity;

import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.util.Constants;
import tech.metavm.util.ContextUtil;

public interface IInstanceContextFactory {

    IInstanceContext newContext(long tenantId);

    IInstanceContext newContext(long tenantId, boolean asyncProcessLogs);

    IInstanceContext newRootContext();

    InstanceContextBuilder newBuilder();

    IEntityContext newEntityContext(long tenantId);

    IEntityContext newEntityContext(long tenantId, boolean asyncProcessing);

    default IEntityContext newEntityContext(boolean asyncProcessing) {
        return newEntityContext(ContextUtil.getTenantId(), asyncProcessing);
    }

    default IEntityContext newRootEntityContext(boolean asyncProcessing) {
        return newEntityContext(Constants.ROOT_TENANT_ID, asyncProcessing);
    }

}
