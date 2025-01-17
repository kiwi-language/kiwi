package org.metavm.entity;

import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.GlobalKlassTagAssigner;
import org.metavm.object.type.StdAllocators;
import org.metavm.object.type.TypeTagStore;
import org.metavm.task.SchedulerRegistry;
import org.metavm.util.InternalException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.metavm.util.Constants.ROOT_APP_ID;

@Component
public class Bootstrap extends EntityContextFactoryAware implements InitializingBean {

    private final StdAllocators stdAllocators;
    private final ColumnStore columnStore;
    private final TypeTagStore typeTagStore;
    private Set<Field> fieldBlacklist = Set.of();
    private Set<Class<?>> classBlackList= Set.of();

    public Bootstrap(EntityContextFactory entityContextFactory, StdAllocators stdAllocators, ColumnStore columnStore, TypeTagStore typeTagStore) {
        super(entityContextFactory);
        this.stdAllocators = stdAllocators;
        this.columnStore = columnStore;
        this.typeTagStore = typeTagStore;
    }

    public BootstrapResult boot() {
        var result = Bootstraps.boot(stdAllocators, columnStore, typeTagStore, classBlackList, fieldBlacklist, true);
        entityContextFactory.setDefContext(result.defContext());
        return result;
    }

    @Transactional
    public void bootAndSave() {
        boot();
        save(true);
    }

    @Transactional
    public void save(boolean saveIds) {
        SystemDefContext defContext = (SystemDefContext) ModelDefRegistry.getDefContext();
        try (var ignoredEntry = defContext.getProfiler().enter("Bootstrap.save")) {
            if (defContext.isFinished())
                return;
            try (var tempContext = entityContextFactory.newContext(ROOT_APP_ID, builder -> builder.timeout(0))) {
                Objects.requireNonNull(defContext).increaseVersionsForAll();
                defContext.finish();
                defContext.getIdentityMap().forEach((object, javaConstruct) -> {
                    if (object.isDurable()) {
                        if (object.isRoot())
                            stdAllocators.putId(javaConstruct, object.getId(), object.getNextNodeId());
                        else
                            stdAllocators.putId(javaConstruct, object.getId());
                    }
                });
                if (saveIds) {
                    stdAllocators.save();
                    columnStore.save();
                    typeTagStore.save();
                }
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
        for (var instance : defContext.entities()) {
            if (instance.isDurable() && !instance.isInlineValue() && instance.tryGetTreeId() == null)
                throw new InternalException("Detected a durable instance with uninitialized id. instance: " + instance);
        }
    }

    public void setFieldBlacklist(Set<Field> fieldBlacklist) {
        this.fieldBlacklist = new HashSet<>(fieldBlacklist);
    }

    public void setClassBlacklist(Set<Class<?>> classBlacklist) {
        this.classBlackList = new HashSet<>(classBlacklist);
    }

    @Override
    public void afterPropertiesSet() {
        boot();
    }
}
