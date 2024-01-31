package tech.metavm.autograph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.DefContext;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.ReadonlyArray;
import tech.metavm.object.instance.core.EntityInstanceContextBridge;
import tech.metavm.object.type.*;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import static tech.metavm.util.Constants.ROOT_APP_ID;

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
        boot = true;
        ContextUtil.setAppId(ROOT_APP_ID);
        var bridge = new EntityInstanceContextBridge();
        var standardInstanceContext = contextFactory.newBridgedInstanceContext(ROOT_APP_ID, bridge,
                new BootIdProvider(stdAllocators));
        contextFactory.setStdContext(standardInstanceContext);
        var defContext = new DefContext(
                stdAllocators::getId,
                standardInstanceContext, columnStore);
        bridge.setEntityContext(defContext);
        ModelDefRegistry.setDefContext(defContext);
        contextFactory.setDefContext(defContext);
        for (Class<?> entityClass : EntityUtils.getModelClasses()) {
            if (!ReadonlyArray.class.isAssignableFrom(entityClass) && !entityClass.isAnonymousClass())
                defContext.getDef(entityClass);
        }
        defContext.flushAndWriteInstances();
        var idNullInstances = NncUtils.filter(defContext.instances(), inst -> inst.tryGetPhysicalId() == null);
        if (!idNullInstances.isEmpty())
            LOGGER.warn(idNullInstances.size() + " instances have null ids. Save is required");
        ContextUtil.clearContextInfo();
    }

}
