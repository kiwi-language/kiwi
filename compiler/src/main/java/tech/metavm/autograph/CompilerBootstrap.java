package tech.metavm.autograph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.object.instance.core.EntityInstanceContextBridge;
import tech.metavm.object.type.*;
import tech.metavm.util.Constants;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import static tech.metavm.util.Constants.ROOT_APP_ID;
import static tech.metavm.util.Constants.getRootAppId;

public class CompilerBootstrap {

    public static final Logger LOGGER = LoggerFactory.getLogger(CompilerBootstrap.class);

    private final CompilerInstanceContextFactory contextFactory;
    private final StdAllocators stdAllocators;// = new StdAllocators(new DirectoryAllocatorStore("/not_exist"));
    private final ColumnStore columnStore = new FileColumnStore("/not_exist");
    private volatile boolean boot;

    CompilerBootstrap(CompilerInstanceContextFactory contextFactory, AllocatorStore allocatorStore) {
        this.contextFactory = contextFactory;
        stdAllocators = new StdAllocators(allocatorStore);
    }

    public synchronized void boot() {
        if(boot)
            throw new IllegalStateException("Already boot");
        try(var ignored = ContextUtil.getProfiler().enter("CompilerBootstrap.boot")) {
            boot = true;
            ContextUtil.setAppId(Constants.getRootAppId());
            var bridge = new EntityInstanceContextBridge();
            var identityContext = new IdentityContext();
            var idInitializer = new BootIdInitializer(new BootIdProvider(stdAllocators), identityContext);
            var standardInstanceContext = contextFactory.newBridgedInstanceContext(getRootAppId(), bridge,
                    idInitializer);
            contextFactory.setStdContext(standardInstanceContext);
            var defContext = new DefContext(
                    new StdIdProvider(new AllocatorStdIdStore(stdAllocators)),
                    standardInstanceContext, columnStore, identityContext);
            bridge.setEntityContext(defContext);
            ModelDefRegistry.setDefContext(defContext);
            contextFactory.setDefContext(defContext);
            for (Class<?> entityClass : EntityUtils.getModelClasses()) {
                if (!ReadonlyArray.class.isAssignableFrom(entityClass) && !entityClass.isAnonymousClass())
                    defContext.getDef(entityClass);
            }
            defContext.flushAndWriteInstances();
            var idNullInstances = NncUtils.filter(defContext.instances(), inst -> inst.tryGetPhysicalId() == null);
            if (!idNullInstances.isEmpty()) {
                LOGGER.warn(idNullInstances.size() + " instances have null ids. Save is required");
                var inst = idNullInstances.get(0);
                if(inst.getMappedEntity() != null)
                    LOGGER.warn("First instance with null id: " + inst.getMappedEntity().getClass().getName());
            }
            ContextUtil.resetLoginInfo();
        }
    }

}
