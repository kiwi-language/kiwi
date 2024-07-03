package org.metavm.entity;

import org.metavm.object.instance.MetaVersionPlugin;
import org.metavm.object.instance.core.EntityInstanceContextBridge;
import org.metavm.object.instance.core.InstanceContext;
import org.metavm.object.type.*;
import org.metavm.task.SchedulerRegistry;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.metavm.util.Constants.ROOT_APP_ID;

@Component
public class Bootstrap extends EntityContextFactoryAware implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    private final StdAllocators stdAllocators;
    private final ColumnStore columnStore;
    private final TypeTagStore typeTagStore;
    private final StdIdStore stdIdStore;
    private final Set<Field> fieldBlacklist = new HashSet<>();

    public Bootstrap(EntityContextFactory entityContextFactory, StdAllocators stdAllocators, ColumnStore columnStore, TypeTagStore typeTagStore, StdIdStore stdIdStore) {
        super(entityContextFactory);
        this.stdAllocators = stdAllocators;
        this.columnStore = columnStore;
        this.stdIdStore = stdIdStore;
        this.typeTagStore = typeTagStore;
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
                    idInitializer, bridge, null, null, null);
            var defContext = new DefContext(
                    new StdIdProvider(stdIdStore),
                    standardInstanceContext, columnStore, typeTagStore, identityContext);
            defContext.setFieldBlacklist(fieldBlacklist);
            bridge.setEntityContext(defContext);
            ModelDefRegistry.setDefContext(defContext);
            entityContextFactory.setDefContext(defContext);
            var entityClasses = EntityUtils.getModelClasses();
            for (ResolutionStage stage : ResolutionStage.values()) {
                for (Class<?> entityClass : entityClasses) {
                    if (!ReadonlyArray.class.isAssignableFrom(entityClass) && !entityClass.isAnonymousClass())
                        defContext.getDef(entityClass, stage);
                }
            }
            defContext.postProcess();
            defContext.flushAndWriteInstances();
            ModelDefRegistry.setDefContext(defContext);
            var idNullInstances = NncUtils.filter(defContext.instances(), inst -> inst.isDurable() && !inst.isValue() && inst.tryGetTreeId() == null);
            if (!idNullInstances.isEmpty()) {
                logger.warn(idNullInstances.size() + " instances have null ids. Save is required");
                if (DebugEnv.bootstrapVerbose) {
                    for (int i = 0; i < 10; i++) {
                        var inst = idNullInstances.get(i);
                        logger.warn("instance with null id: {}, identity: {}", Instances.getInstancePath(inst),
                                identityContext.getModelId(inst.getMappedEntity()));
                    }
                }
            }
            ContextUtil.clearContextInfo();
            return new BootstrapResult(idNullInstances.size());
        } finally {
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
//                var klass = defContext.getKlass(Constraint.class);
//                DebugEnv.instance = defContext.getInstance(klass.getDefaultMapping());
                var stdInstanceContext = (InstanceContext) defContext.getInstanceContext();
                var metaVersionPlugin = stdInstanceContext.getPlugin(MetaVersionPlugin.class);
                var bridge = new EntityInstanceContextBridge();
                bridge.setEntityContext(tempContext);
                metaVersionPlugin.setVersionRepository(bridge);
                NncUtils.requireNonNull(defContext.getInstanceContext()).increaseVersionsForAll();
                defContext.finish();
                defContext.getIdentityMap().forEach((object, javaConstruct) -> {
                    if (EntityUtils.isDurable(object)) {
                        var instance = defContext.getInstance(object);
                        if (instance.isRoot())
                            stdAllocators.putId(javaConstruct, instance.getId(), instance.getNextNodeId());
                        else
                            stdAllocators.putId(javaConstruct, instance.getId());
                    }
                });
                if (saveIds) {
                    stdAllocators.save();
                    columnStore.save();
                    typeTagStore.save();
                }
                stdIdStore.save(defContext.getStdIdMap());
                ensureIdInitialized();
                tempContext.finish();
            }
        }
    }

    @Transactional
    public void initSystemEntities() {
        try(var context = newPlatformContext()) {
            SchedulerRegistry.initialize(context);
            GlobalKlassTagAssigner.initialize(context);
            context.finish();
        }
    }

    private void ensureIdInitialized() {
        var defContext = ModelDefRegistry.getDefContext();
        for (var instance : defContext.instances()) {
            if (instance.isDurable() && !instance.isValue() && instance.tryGetTreeId() == null)
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
