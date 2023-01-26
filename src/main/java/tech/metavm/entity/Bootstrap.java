package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.BootIdProvider;
import tech.metavm.object.meta.StdAllocators;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.List;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;

@Component
public class Bootstrap implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    private final InstanceContextFactory instanceContextFactory;
    private final StdAllocators stdAllocators;

    public Bootstrap(InstanceContextFactory instanceContextFactory, StdAllocators stdAllocators) {
        this.instanceContextFactory = instanceContextFactory;
        this.stdAllocators = stdAllocators;
    }

    @Transactional
    public void bootAndSave() {
        boot();
        save(true);
    }

    public void boot() {
        ContextUtil.setContextInfo(ROOT_TENANT_ID, -1L);
        InstanceContext standardInstanceContext = instanceContextFactory.newContext(
                ROOT_TENANT_ID, false, new BootIdProvider(stdAllocators), null, null
        );
        InstanceContextFactory.setStdContext(standardInstanceContext);

        DefContext defContext = new DefContext(
                stdAllocators::getId,
                standardInstanceContext);
        standardInstanceContext.setEntityContext(defContext);
        ModelDefRegistry.setDefContext(defContext);

        ReflectUtils.getModelClasses().forEach(defContext::getDef);
        defContext.flushAndWriteInstances();
        ModelDefRegistry.setDefContext(defContext);

        List<Instance> idNullInstances = NncUtils.filter(defContext.instances(), inst -> inst.getId() == null);
        if(!idNullInstances.isEmpty()) {
            LOGGER.warn(idNullInstances.size() + " instances have null ids. Save is required");
        }
    }

    @Transactional
    public void save(boolean saveIds) {
        DefContext defContext = ModelDefRegistry.getDefContext();
        if(defContext.isFinished()) {
            return;
        }
        NncUtils.requireNonNull(defContext.getInstanceContext()).increaseVersionsForAll();
        defContext.finish();

        defContext.getIdentityMap().forEach((model, javaConstruct) ->
                stdAllocators.putId(javaConstruct, defContext.getInstance(model).getId())
        );
        defContext.getInstanceMapping().forEach((javaConstruct, instance) ->
                stdAllocators.putId(javaConstruct, instance.getId())
        );
//        for (ModelDef<?, ?> def : defContext.getAllDefList()) {
//            def.getEntityMapping().forEach((javaConstruct, entity) ->
//                    stdAllocators.putId(javaConstruct, entity.getId())
//            );
//            def.getInstanceMapping().forEach((javaConstruct, instance) ->
//                    stdAllocators.putId(javaConstruct, instance.getId())
//            );
//        }
        if(saveIds) {
            stdAllocators.save();
        }
        check();
    }

    private void check() {
        DefContext defContext = ModelDefRegistry.getDefContext();
        for (Instance instance : defContext.instances()) {
            if(instance.getId() == null) {
                throw new InternalException("Detected instance with uninitialized id. instance: " + instance);
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        boot();
    }
}
