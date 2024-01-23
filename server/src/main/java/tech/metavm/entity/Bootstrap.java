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
import tech.metavm.util.ContextUtil;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static tech.metavm.util.Constants.ROOT_APP_ID;

@Component
public class Bootstrap extends EntityContextFactoryBean implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private final StdAllocators stdAllocators;
    private final ColumnStore columnStore;
    private final Set<Field> fieldBlacklist = new HashSet<>();

    public Bootstrap(EntityContextFactory entityContextFactory, InstanceContextFactory instanceContextFactory,
                     StdAllocators stdAllocators, ColumnStore columnStore) {
        super(entityContextFactory);
        this.stdAllocators = stdAllocators;
        this.columnStore = columnStore;
    }

    public BootstrapResult boot() {
        ContextUtil.setAppId(ROOT_APP_ID);
        var bridge = new EntityInstanceContextBridge();
        var standardInstanceContext = (InstanceContext) entityContextFactory.newBridgedInstanceContext(
                ROOT_APP_ID, false, null, null,
                new BootIdProvider(stdAllocators), bridge);
        var defContext = new DefContext(
                stdAllocators::getId,
                standardInstanceContext, columnStore);
        defContext.setFieldBlacklist(fieldBlacklist);
        bridge.setEntityContext(defContext);
        InstanceContextFactory.setDefContext(defContext);
//        standardInstanceContext.setDefContext(defContext);
//        standardInstanceContext.setEntityContext(defContext);
        ModelDefRegistry.setDefContext(defContext);
        for (Class<?> entityClass : EntityUtils.getModelClasses()) {
            if (!ReadonlyArray.class.isAssignableFrom(entityClass) && !entityClass.isAnonymousClass())
                defContext.getDef(entityClass);
        }
        defContext.flushAndWriteInstances();
        ModelDefRegistry.setDefContext(defContext);

        var idNullInstances = NncUtils.filter(defContext.instances(), inst -> inst.isDurable() && inst.tryGetPhysicalId() == null);
        if (!idNullInstances.isEmpty())
            LOGGER.warn(idNullInstances.size() + " instances have null ids. Save is required");
        ContextUtil.clearContextInfo();
        return new BootstrapResult(idNullInstances.size());
    }

    @Transactional
    public void bootAndSave() {
        boot();
        save(true);
    }

    @Transactional
    public void save(boolean saveIds) {
        DefContext defContext = ModelDefRegistry.getDefContext();
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
                    stdAllocators.putId(javaConstruct, defContext.getInstance(object).getPhysicalId());
            });
//            defContext.getInstanceMapping().forEach((javaConstruct, instance) -> {
//                if (instance.isDurable())
//                    stdAllocators.putId(javaConstruct, instance.getIdRequired());
//            });
            if (saveIds) {
                stdAllocators.save();
                columnStore.save();
            }
            ensureIdInitialized();
            tempContext.finish();
        }
    }

    private void ensureIdInitialized() {
        var defContext = ModelDefRegistry.getDefContext();
        for (var instance : defContext.instances()) {
            if (instance.isDurable() && instance.tryGetPhysicalId() == null)
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
