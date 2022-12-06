package tech.metavm.entity;

import org.reflections.Reflections;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.object.meta.BootIdProvider;
import tech.metavm.object.meta.StdAllocators;
import tech.metavm.util.Constants;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.Set;

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
    public void boot() {
        ContextUtil.setContextInfo(ROOT_TENANT_ID, -1L);
        InstanceContext standardInstanceContext = instanceContextFactory.newContext(
                ROOT_TENANT_ID, false, new BootIdProvider(stdAllocators), null, null
        );

        /* = new InstanceContext(
                -1L,
                instanceStore,
                new BootIdProvider(stdAllocators),
                Executors.newSingleThreadExecutor(),
                true,
                List.of(),
                null
        );*/
        InstanceContextFactory.setStdContext(standardInstanceContext);

        DefContext defContext = new DefContext(
                stdAllocators::getId,
                standardInstanceContext);
        standardInstanceContext.setEntityContext(defContext);
        ModelDefRegistry.setDefContext(defContext);

        ReflectUtils.getModelClasses().forEach(defContext::getDef);

        defContext.finish();
        ModelDefRegistry.setDefContext(defContext);

        for (ModelDef<?, ?> def : defContext.getAllDefList()) {
            def.getEntityMapping().forEach((javaConstruct, entity) ->
                    stdAllocators.putId(javaConstruct, entity.getId())
            );
            def.getInstanceMapping().forEach((javaConstruct, instance) ->
                    stdAllocators.putId(javaConstruct, instance.getId())
            );
        }

        stdAllocators.save();
    }

    @Transactional
    @Override
    public void afterPropertiesSet() {
//        boot();
    }
}
