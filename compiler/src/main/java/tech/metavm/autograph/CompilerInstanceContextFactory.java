package tech.metavm.autograph;

import tech.metavm.entity.DefContext;
import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.IEntityContext;
import tech.metavm.instance.core.CompilerIdService;
import tech.metavm.instance.core.CompilerInstanceContext;
import tech.metavm.object.instance.core.IInstanceContext;

import java.io.File;
import java.util.List;

public class CompilerInstanceContextFactory {

//    public static final CompilerInstanceContextFactory INSTANCE = new CompilerInstanceContextFactory();

    private final ServerVersionSource versionSource = new ServerVersionSource();
    private final ServerTreeSource serverTreeSource = new ServerTreeSource();
    private final DiskTreeStore diskTreeSource;
    private final EntityIdProvider idService = new CompilerIdService();
    private final LocalIndexSource localIndexSource;

    private IInstanceContext stdContext;
    private DefContext defContext;

    public CompilerInstanceContextFactory(DiskTreeStore diskTreeSource, LocalIndexSource localIndexSource) {
        this.diskTreeSource = diskTreeSource;
        this.localIndexSource = localIndexSource;
    }

    public IInstanceContext newContext(long appId) {
        return new CompilerInstanceContext(
                appId,
                List.of(diskTreeSource, serverTreeSource),
                versionSource,
                idService,
                localIndexSource,
                defContext,
                stdContext,
                false
        );
    }

    public IEntityContext newEntityContext(long appId) {
        //noinspection resource
        return newContext(appId).getEntityContext();
    }

    public void setStdContext(IInstanceContext stdContext) {
        this.stdContext = stdContext;
    }

    public void setDefContext(DefContext defContext) {
        this.defContext = defContext;
    }
}
