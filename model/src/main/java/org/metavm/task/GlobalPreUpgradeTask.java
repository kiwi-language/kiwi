package org.metavm.task;

import org.metavm.application.Application;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.rest.dto.PreUpgradeRequest;
import org.metavm.util.NncUtils;
import org.metavm.util.TriConsumer;

public class GlobalPreUpgradeTask extends GlobalTask {

    public static TriConsumer<PreUpgradeRequest, WAL, IEntityContext> preUpgradeAction;
    private final String requestJSON;
    private final WAL defWAL;

    public GlobalPreUpgradeTask(PreUpgradeRequest preUpgradeRequest, WAL defWAL) {
        super("GlobalPreUpgradeTask");
        requestJSON = NncUtils.toJSONString(preUpgradeRequest);
        this.defWAL = defWAL;
    }

    @Override
    protected void processApplication(IEntityContext context, Application application) {
        preUpgradeAction.accept(getRequest(), defWAL, context);
    }

    private PreUpgradeRequest getRequest() {
        return NncUtils.readJSONString(requestJSON, PreUpgradeRequest.class);
    }

    @Override
    public long getTimeout() {
        return 3000L;
    }
}
