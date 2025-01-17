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
        return newEntityContext(appId);
    }

    public IInstanceContext newEntityContext(long appId) {
        return newBridgedInstanceContext(appId, new DefaultIdInitializer(idService));

    }

    public IInstanceContext newBridgedInstanceContext(long appId, IdInitializer idProvider) {
        return new CompilerInstanceContext(
                appId,
                List.of(diskTreeSource, serverTreeSource),
                versionSource,
                idProvider,
                localIndexSource,
                stdContext,
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
