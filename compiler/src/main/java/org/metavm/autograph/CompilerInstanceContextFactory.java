package org.metavm.autograph;

import org.metavm.entity.*;
import org.metavm.instance.core.CompilerIdService;
import org.metavm.instance.core.CompilerInstanceContext;
import org.metavm.object.instance.core.EntityInstanceContextBridge;
import org.metavm.object.instance.core.IInstanceContext;

import java.util.List;

public class CompilerInstanceContextFactory {

//    public static final CompilerInstanceContextFactory INSTANCE = new CompilerInstanceContextFactory();

    private final ServerVersionSource versionSource;
    private final ServerTreeSource serverTreeSource;
    private final DiskTreeStore diskTreeSource;
    private final EntityIdProvider idService;
    private final LocalIndexSource localIndexSource;

    private IInstanceContext stdContext;
    private SystemDefContext defContext;

    public CompilerInstanceContextFactory(DiskTreeStore diskTreeSource, LocalIndexSource localIndexSource, TypeClient typeClient) {
        this.diskTreeSource = diskTreeSource;
        this.localIndexSource = localIndexSource;
        idService = new CompilerIdService();
        versionSource = new ServerVersionSource(typeClient);
        serverTreeSource = new ServerTreeSource(typeClient);
    }

    public IInstanceContext newContext(long appId) {
        //noinspection resource
        return newEntityContext(appId).getInstanceContext();
    }

    public IEntityContext newEntityContext(long appId) {
        var bridge = new EntityInstanceContextBridge();
        var context = newBridgedInstanceContext(appId, bridge, new DefaultIdInitializer(idService));
        var entityContext = new CompilerEntityContext(context, defContext, defContext);
        bridge.setEntityContext(entityContext);
        return entityContext;
    }

    public IInstanceContext newBridgedInstanceContext(long appId, EntityInstanceContextBridge bridge, IdInitializer idProvider) {
        return new CompilerInstanceContext(
                appId,
                List.of(diskTreeSource, serverTreeSource),
                versionSource,
                idProvider,
                localIndexSource,
                stdContext,
                bridge,
                bridge,
                bridge,
                false
        );
    }

    public void setStdContext(IInstanceContext stdContext) {
        this.stdContext = stdContext;
    }

    public void setDefContext(SystemDefContext defContext) {
        this.defContext = defContext;
    }
}
