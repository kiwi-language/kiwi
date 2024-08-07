package org.metavm.task;

import org.metavm.application.Application;
import org.metavm.entity.IEntityContext;
import org.metavm.object.type.rest.dto.PreUpgradeRequest;
import org.metavm.util.NncUtils;

import java.util.function.BiConsumer;

public class GlobalPreUpgradeTask extends GlobalTask {

    public static BiConsumer<PreUpgradeRequest, IEntityContext> preUpgradeAction;
    private final String requestJSON;

    public GlobalPreUpgradeTask(PreUpgradeRequest preUpgradeRequest) {
        super("GlobalPreUpgradeTask");
        requestJSON = NncUtils.toJSONString(preUpgradeRequest);
    }

    @Override
    protected void processApplication(IEntityContext context, Application application) {
        preUpgradeAction.accept(getRequest(), context);
    }

    private PreUpgradeRequest getRequest() {
        return NncUtils.readJSONString(requestJSON, PreUpgradeRequest.class);
    }

}
