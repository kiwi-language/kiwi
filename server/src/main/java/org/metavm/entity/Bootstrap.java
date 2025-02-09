package org.metavm.entity;

import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.GlobalKlassTagAssigner;
import org.metavm.object.type.StdAllocators;
import org.metavm.object.type.TypeTagStore;
import org.metavm.task.SchedulerRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

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
    public void initSystemEntities() {
        try(var context = newPlatformContext()) {
            SchedulerRegistry.initialize(context);
            GlobalKlassTagAssigner.initialize(context);
            context.finish();
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
