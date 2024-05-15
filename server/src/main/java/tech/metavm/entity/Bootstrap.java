package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.object.instance.MetaVersionPlugin;
import tech.metavm.object.instance.core.EntityInstanceContextBridge;
import tech.metavm.object.instance.core.InstanceContext;
import tech.metavm.object.type.BootIdProvider;
import tech.metavm.object.type.ColumnStore;
import tech.metavm.object.type.StdAllocators;
import tech.metavm.util.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static tech.metavm.util.Constants.ROOT_APP_ID;

@Component
public class Bootstrap extends EntityContextFactoryBean implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    private final StdAllocators stdAllocators;
    private final ColumnStore columnStore;
    private final StdIdStore stdIdStore;
    private final Set<Field> fieldBlacklist = new HashSet<>();

    public Bootstrap(EntityContextFactory entityContextFactory, StdAllocators stdAllocators, ColumnStore columnStore, StdIdStore stdIdStore) {
        super(entityContextFactory);
        this.stdAllocators = stdAllocators;
        this.columnStore = columnStore;
        this.stdIdStore = stdIdStore;
    }

    public BootstrapResult boot() {
        try (var ignoredEntry = ContextUtil.getProfiler().enter("Bootstrap.boot")) {
            ThreadConfigs.sharedParameterizedElements(true);
            ContextUtil.setAppId(ROOT_APP_ID);
            var identityContext = new IdentityContext();
            var idInitializer = new BootIdInitializer(new BootIdProvider(stdAllocators), identityContext);
            var bridge = new EntityInstanceContextBridge();
            var standardInstanceContext = (InstanceContext) entityContextFactory.newBridgedInstanceContext(
                    ROOT_APP_ID, false, null, null,
                    idInitializer, bridge);
            var defContext = new DefContext(
                    new StdIdProvider(stdIdStore),
                    standardInstanceContext, columnStore, identityContext);
            defContext.setFieldBlacklist(fieldBlacklist);
            bridge.setEntityContext(defContext);
            ModelDefRegistry.setDefContext(defContext);
            entityContextFactory.setDefContext(defContext);
            for (Class<?> entityClass : EntityUtils.getModelClasses()) {
                if (!ReadonlyArray.class.isAssignableFrom(entityClass) && !entityClass.isAnonymousClass())
                    defContext.getDef(entityClass);
            }
            defContext.flushAndWriteInstances();
            ModelDefRegistry.setDefContext(defContext);
            var idNullInstances = NncUtils.filter(defContext.instances(), inst -> inst.isDurable() && inst.tryGetTreeId() == null);
            if (!idNullInstances.isEmpty()) {
                logger.warn(idNullInstances.size() + " instances have null ids. Save is required");
                if(DebugEnv.bootstrapVerbose) {
                    for (int i = 0; i < 10; i++) {
                        var inst = idNullInstances.get(i);
                        logger.warn("instance with null id: {}, identity: {}", Instances.getInstancePath(inst),
                                identityContext.getModelId(inst.getMappedEntity()));
                    }
                }
            }
            ContextUtil.clearContextInfo();
            return new BootstrapResult(idNullInstances.size());
        }
        finally {
            ThreadConfigs.sharedParameterizedElements(false);
        }
    }

    @Transactional
    public void bootAndSave() {
        boot();
        save(true);
    }

    @Transactional
    public void save(boolean saveIds) {
        DefContext defContext = ModelDefRegistry.getDefContext();
        try (var ignoredEntry = defContext.getProfiler().enter("Bootstrap.save")) {
            if (defContext.isFinished())
                return;
            try (var tempContext = newContext(ROOT_APP_ID)) {
                var stdInstanceContext = (InstanceContext) defContext.getInstanceContext();
                var metaVersionPlugin = stdInstanceContext.getPlugin(MetaVersionPlugin.class);
                var bridge = new EntityInstanceContextBridge();
                bridge.setEntityContext(tempContext);
                metaVersionPlugin.setVersionRepository(bridge);
                NncUtils.requireNonNull(defContext.getInstanceContext()).increaseVersionsForAll();
                defContext.finish();
                defContext.getIdentityMap().forEach((object, javaConstruct) -> {
                    if (EntityUtils.isDurable(object))
                        stdAllocators.putId(javaConstruct, defContext.getInstance(object).getId());
                });
                if (saveIds) {
                    stdAllocators.save();
                    columnStore.save();
                }
                stdIdStore.save(defContext.getStdIdMap());
                ensureIdInitialized();
                tempContext.finish();
            }
        }
    }

    private void ensureIdInitialized() {
        var defContext = ModelDefRegistry.getDefContext();
        for (var instance : defContext.instances()) {
            if (instance.isDurable() && instance.tryGetTreeId() == null)
                throw new InternalException("Detected a durable instance with uninitialized id. instance: " + instance);
        }
    }

    public void setFieldBlacklist(Set<Field> fieldBlacklist) {
        this.fieldBlacklist.addAll(fieldBlacklist);
    }

    @Override
    public void afterPropertiesSet() {
        boot();
    }
}
