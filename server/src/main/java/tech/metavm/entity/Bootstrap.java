package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.object.type.BootIdProvider;
import tech.metavm.object.type.ColumnStore;
import tech.metavm.object.type.StdAllocators;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;

import static tech.metavm.util.Constants.ROOT_APP_ID;

@Component
public class Bootstrap implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private final InstanceContextFactory instanceContextFactory;
    private final StdAllocators stdAllocators;
    private final ColumnStore columnStore;

    public Bootstrap(InstanceContextFactory instanceContextFactory, StdAllocators stdAllocators, ColumnStore columnStore) {
        this.instanceContextFactory = instanceContextFactory;
        this.stdAllocators = stdAllocators;
        this.columnStore = columnStore;
    }

    public void boot() {
        ContextUtil.setAppId(ROOT_APP_ID);
        InstanceContext standardInstanceContext = (InstanceContext) instanceContextFactory.newContext(
                ROOT_APP_ID, false, false, new BootIdProvider(stdAllocators), null, null
        );
        InstanceContextFactory.setStdContext(standardInstanceContext);
        DefContext defContext = new DefContext(
                stdAllocators::getId,
                standardInstanceContext, columnStore);
        standardInstanceContext.setDefContext(defContext);
        standardInstanceContext.setEntityContext(defContext);
        ModelDefRegistry.setDefContext(defContext);
        for (Class<?> entityClass : EntityUtils.getModelClasses()) {
            if (!ReadonlyArray.class.isAssignableFrom(entityClass) && !entityClass.isAnonymousClass())
                defContext.getDef(entityClass);
        }
        defContext.flushAndWriteInstances();
        ModelDefRegistry.setDefContext(defContext);

        List<Instance> idNullInstances = NncUtils.filter(defContext.instances(), inst -> inst.getId() == null);
        if (!idNullInstances.isEmpty()) {
            LOGGER.warn(idNullInstances.size() + " instances have null ids. Save is required");
        }
        ContextUtil.clearContextInfo();
    }

    @Transactional
    public void bootAndSave() {
        boot();
        save(true);
    }

    @Transactional
    public void save(boolean saveIds) {
        DefContext defContext = ModelDefRegistry.getDefContext();
        if (defContext.isFinished()) {
            return;
        }
        try (IEntityContext tempContext = instanceContextFactory.newEntityContext(ROOT_APP_ID)) {
            NncUtils.requireNonNull(defContext.getInstanceContext()).increaseVersionsForAll();
            defContext.finish();
            defContext.getIdentityMap().forEach((model, javaConstruct) ->
                    stdAllocators.putId(javaConstruct, defContext.getInstance(model).getIdRequired())
            );
            defContext.getInstanceMapping().forEach((javaConstruct, instance) ->
                    stdAllocators.putId(javaConstruct, instance.getIdRequired())
            );
            if (saveIds) {
                stdAllocators.save();
                columnStore.save();
            }
            check();
            tempContext.finish();
        }
    }

    private void check() {
        DefContext defContext = ModelDefRegistry.getDefContext();
        for (Instance instance : defContext.instances()) {
            if (instance.getId() == null) {
                throw new InternalException("Detected instance with uninitialized id. instance: " + instance);
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        boot();
    }
}
