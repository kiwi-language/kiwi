package org.metavm.autograph;

import org.metavm.entity.*;
import org.metavm.object.instance.core.EntityInstanceContextBridge;
import org.metavm.object.type.*;
import org.metavm.util.Constants;
import org.metavm.util.ContextUtil;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.metavm.util.Constants.ROOT_APP_ID;

public class CompilerBootstrap {

    public static final Logger logger = LoggerFactory.getLogger(CompilerBootstrap.class);

    private final CompilerInstanceContextFactory contextFactory;
    private final StdAllocators stdAllocators;
    private final ColumnStore columnStore;
    private final TypeTagStore typeTagStore;
    private volatile boolean boot;

    CompilerBootstrap(CompilerInstanceContextFactory contextFactory, AllocatorStore allocatorStore, ColumnStore columnStore, TypeTagStore typeTagStore) {
        this.contextFactory = contextFactory;
        stdAllocators = new StdAllocators(allocatorStore);
        this.columnStore = columnStore;
        this.typeTagStore = typeTagStore;
    }

    public synchronized void boot() {
        if(boot)
            throw new IllegalStateException("Already boot");
        try(var ignored = ContextUtil.getProfiler().enter("CompilerBootstrap.boot")) {
            boot = true;
            ContextUtil.setAppId(Constants.ROOT_APP_ID);
            var bridge = new EntityInstanceContextBridge();
            var identityContext = new IdentityContext();
            var idInitializer = new BootIdInitializer(new BootIdProvider(stdAllocators), identityContext);
            var standardInstanceContext = contextFactory.newBridgedInstanceContext(ROOT_APP_ID, bridge,
                    idInitializer);
            contextFactory.setStdContext(standardInstanceContext);
            var defContext = new DefContext(
                    new StdIdProvider(new AllocatorStdIdStore(stdAllocators)),
                    standardInstanceContext, columnStore, typeTagStore, identityContext);
            bridge.setEntityContext(defContext);
            ModelDefRegistry.setDefContext(defContext);
            contextFactory.setDefContext(defContext);
            for (Class<?> entityClass : EntityUtils.getModelClasses()) {
                if (!ReadonlyArray.class.isAssignableFrom(entityClass) && !entityClass.isAnonymousClass())
                    defContext.getDef(entityClass);
            }
            defContext.postProcess();
            defContext.flushAndWriteInstances();
            var idNullInstances = NncUtils.filter(defContext.instances(), inst -> inst.tryGetTreeId() == null);
            if (!idNullInstances.isEmpty()) {
                logger.warn(idNullInstances.size() + " instances have null ids. Save is required");
                var inst = idNullInstances.get(0);
                if(inst.getMappedEntity() != null)
                    logger.warn("First instance with null id: " + inst.getMappedEntity().getClass().getName());
            }
            ContextUtil.resetLoginInfo();
        }
    }

}
