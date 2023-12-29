package tech.metavm.autograph;

import tech.metavm.entity.CompilerEntityContext;
import tech.metavm.entity.DefContext;
import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.IEntityContext;
import tech.metavm.instance.core.CompilerIdService;
import tech.metavm.instance.core.CompilerInstanceContext;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.InstanceContextDependency;

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
        //noinspection resource
        return newEntityContext(appId).getInstanceContext();
    }

    public IEntityContext newEntityContext(long appId) {
        var dependency = new InstanceContextDependency();
        var context = new CompilerInstanceContext(
                appId,
                List.of(diskTreeSource, serverTreeSource),
                versionSource,
                idService,
                localIndexSource,
                defContext,
                stdContext,
                dependency,
                dependency,
                dependency,
                false
        );
        var entityContext = new CompilerEntityContext(context, defContext, defContext);
        dependency.setEntityContext(entityContext);
        return entityContext;
    }

    public void setStdContext(IInstanceContext stdContext) {
        this.stdContext = stdContext;
    }

    public void setDefContext(DefContext defContext) {
        this.defContext = defContext;
    }
}
