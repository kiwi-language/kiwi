package tech.metavm.entity;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.object.meta.BootIdProvider;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.object.meta.StdAllocators;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.ReflectUtils;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;

@Component
public class Bootstrap implements InitializingBean {

    private final InstanceContextFactory instanceContextFactory;
    private final StdAllocators stdAllocators;

    public Bootstrap(InstanceContextFactory instanceContextFactory, StdAllocators stdAllocators) {
        this.instanceContextFactory = instanceContextFactory;
        this.stdAllocators = stdAllocators;
    }

    @Transactional
    public void bootAndSave() {
        boot();
        save();
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
        defContext.flush();
        ModelDefRegistry.setDefContext(defContext);
    }

    @Transactional
    public void save() {
        if(ModelDefRegistry.getDefContext().isFinished()) {
            return;
        }
        ModelDefRegistry.getDefContext().finish();
        for (ModelDef<?, ?> def : ModelDefRegistry.getDefContext().getAllDefList()) {
            def.getEntityMapping().forEach((javaConstruct, entity) ->
                    stdAllocators.putId(javaConstruct, entity.getId())
            );
            def.getInstanceMapping().forEach((javaConstruct, instance) ->
                    stdAllocators.putId(javaConstruct, instance.getId())
            );
        }
        stdAllocators.save();
    }

    @Override
    public void afterPropertiesSet() {
        boot();
    }
}
