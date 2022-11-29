package tech.metavm.entity;

import org.reflections.Reflections;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.object.instance.IInstanceStore;
import tech.metavm.object.meta.StdAllocators;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class Bootstrap {

    private final InstanceContext instanceContext;
    private final StdAllocators stdAllocators;
    private DefContext defContext;

    public Bootstrap(IInstanceStore instanceStore, String idSaveDir) {
        this.stdAllocators = new StdAllocators(idSaveDir);
        this.instanceContext = new InstanceContext(
                -1L,
                instanceStore,
                stdAllocators,
                Executors.newSingleThreadExecutor(),
                true,
                List.of(),
                RootInstanceContext.getInstance(),
                RootRegistry.getDefContext()
        );
        InstanceContextFactory.initStdContext(instanceContext);
    }

    @Transactional
    public void boot() {
        ContextUtil.setContextInfo(-1L, -1L);
        Reflections reflections = new Reflections("tech.metavm");
        defContext = new DefContext(
                jc -> NncUtils.get(stdAllocators.getId(jc), instanceContext::get),
                instanceContext.getEntityContext()
        );
        EntityTypeRegistry.setDefContext(defContext);

        Set<Class<? extends Entity>> entitySubTypes = reflections.getSubTypesOf(Entity.class);
        Set<Class<?>> entityTypes = reflections.getTypesAnnotatedWith(EntityType.class);
        Set<Class<?>> valueTypes = reflections.getTypesAnnotatedWith(ValueType.class);
        Set<Class<?>> types = NncUtils.mergeSets(entitySubTypes, entityTypes, valueTypes);
        types.forEach(defContext::getDef);

        for (ModelDef<?, ?> def : defContext.getAllDefs()) {
            def.getEntityMapping().forEach((javaConstruct, entity) -> {
                if(entity.getId() == null) {
                    instanceContext.getEntityContext().bind(entity);
                }
            });
            def.getInstanceMapping().forEach((javaConstruct, instance) -> {
                if(instance.getId() == null) {
                    instanceContext.bind(instance);
                }
            });
        }

        instanceContext.getEntityContext().finish();

        for (ModelDef<?, ?> def : defContext.getAllDefs()) {
            def.getEntityMapping().forEach((javaConstruct, entity) ->
                    stdAllocators.putId(javaConstruct, entity.getId())
            );
            def.getInstanceMapping().forEach((javaConstruct, instance) ->
                    stdAllocators.putId(javaConstruct, instance.getId())
            );
        }
        stdAllocators.save();
    }

    public DefContext getDefContext() {
        return defContext;
    }
}
