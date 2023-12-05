package tech.metavm.entity;

import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.util.Constants;
import tech.metavm.util.ContextUtil;

public interface IInstanceContextFactory {

    IInstanceContext newContext(long appId);

    IInstanceContext newContext(long appId, boolean asyncProcessLogs);

    InstanceContextBuilder newBuilder();

    IEntityContext newEntityContext(long appId);

    IEntityContext newEntityContext(long appId, boolean asyncProcessing);

    default IEntityContext newEntityContext(boolean asyncProcessing) {
        return newEntityContext(ContextUtil.getAppId(), asyncProcessing);
    }

    default IEntityContext newRootEntityContext(boolean asyncProcessing) {
        return newEntityContext(Constants.ROOT_APP_ID, asyncProcessing);
    }

}
