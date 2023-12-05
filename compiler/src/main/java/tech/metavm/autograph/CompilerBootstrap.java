package tech.metavm.autograph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.DefContext;
import tech.metavm.entity.EntityUtils;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.ReadonlyArray;
import tech.metavm.instance.core.CompilerInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ColumnStore;
import tech.metavm.object.type.DirectoryAllocatorStore;
import tech.metavm.object.type.FileColumnStore;
import tech.metavm.object.type.StdAllocators;
import tech.metavm.util.Constants;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.List;

import static tech.metavm.util.Constants.ROOT_APP_ID;

public class CompilerBootstrap {

    public static final Logger LOGGER = LoggerFactory.getLogger(CompilerBootstrap.class);

    private final CompilerInstanceContextFactory contextFactory;
    private final StdAllocators stdAllocators = new StdAllocators(new DirectoryAllocatorStore("/not_exist"));
    private final ColumnStore columnStore = new FileColumnStore("/not_exist");
    private volatile boolean boot;

    CompilerBootstrap(CompilerInstanceContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public synchronized void boot() {
        if(boot)
            throw new IllegalStateException("Already boot");
        boot = true;
        ContextUtil.setAppId(ROOT_APP_ID);
        CompilerInstanceContext standardInstanceContext = (CompilerInstanceContext) contextFactory.newContext(
                Constants.ROOT_APP_ID
        );
        contextFactory.setStdContext(standardInstanceContext);
        DefContext defContext = new DefContext(
                stdAllocators::getId,
                standardInstanceContext, columnStore);
        ModelDefRegistry.setDefContext(defContext);
        standardInstanceContext.setDefContext(defContext);
        standardInstanceContext.setEntityContext(defContext);
        contextFactory.setDefContext(defContext);
        for (Class<?> entityClass : EntityUtils.getModelClasses()) {
            if (!ReadonlyArray.class.isAssignableFrom(entityClass) && !entityClass.isAnonymousClass())
                defContext.getDef(entityClass);
        }
        defContext.flushAndWriteInstances();

        List<Instance> idNullInstances = NncUtils.filter(defContext.instances(), inst -> inst.getId() == null);
        if (!idNullInstances.isEmpty())
            LOGGER.warn(idNullInstances.size() + " instances have null ids. Save is required");
        ContextUtil.clearContextInfo();
    }

}
