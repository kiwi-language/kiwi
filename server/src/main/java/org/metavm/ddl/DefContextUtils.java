package org.metavm.ddl;

import org.metavm.entity.*;
import org.metavm.object.instance.core.EntityInstanceContextBridge;
import org.metavm.object.instance.core.InstanceContext;
import org.metavm.object.instance.core.WAL;
import org.metavm.util.Constants;

import java.util.List;

public class DefContextUtils {

    public static DefContext createReversedDefContext(WAL wal, EntityContextFactory entityContextFactory, List<String> extraKlassIds) {
        var sysDefContext = ModelDefRegistry.getDefContext();
        var bridge = new EntityInstanceContextBridge();
        var standardInstanceContext = (InstanceContext) entityContextFactory.newBridgedInstanceContext(
                Constants.ROOT_APP_ID, false, null, null,
                new DefaultIdInitializer((i, j) -> {throw new UnsupportedOperationException();}),
                bridge, wal, null, null, false,
                builder -> builder.timeout(0L).typeDefProvider(sysDefContext)
        );
        var defContext = new ReversedDefContext(standardInstanceContext, sysDefContext);
        bridge.setEntityContext(defContext);
        defContext.initializeFrom(sysDefContext, extraKlassIds);
        ModelDefRegistry.setLocalDefContext(defContext);
        return defContext;
    }

}
