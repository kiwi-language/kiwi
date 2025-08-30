package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.beans.BeanDefinition;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.beans.ComponentBeanDefinition;
import org.metavm.beans.FactoryBeanDefinition;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.IInstanceContext;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class BeanManager {

    public void createBeans(Collection<Klass> klasses, BeanDefinitionRegistry registry, IInstanceContext context) {
        var existingBeanDefs = new ArrayList<>(registry.getBeanDefinitions());
        var defs = new ArrayList<BeanDefinition>();
        for (Klass k : klasses)
            createBeanDefinitions(k, defs, registry, context);
        defs.forEach(registry::registerBeanDefinition);
        initializeBeanDefinitions(defs, registry, context);
        existingBeanDefs.forEach(def -> def.updateBean(registry));
    }

    public void removeBeans(Collection<Klass> klasses, BeanDefinitionRegistry registry, IInstanceContext context) {
        var removedDefs = removeBeanDefs(klasses, registry);
        for (var bd : removedDefs) {
            destructBean(bd, context);
        }
    }

    private List<BeanDefinition> removeBeanDefs(Collection<Klass> klasses, BeanDefinitionRegistry registry) {
        var removedDefs = new ArrayList<BeanDefinition>();
        klasses.forEach(k -> {
            removedDefs.addAll(registry.removeBeanDefByType(k.getType()));
            var beanKind = k.getAttribute(AttributeNames.BEAN_KIND);
            if (BeanKinds.CONFIGURATION.equals(beanKind)) {
                for (Method method : k.getMethods()) {
                    if (method.getAttribute(AttributeNames.BEAN_NAME) != null) {
                        removedDefs.addAll(registry.removeBeanDefByFactoryMethod(method));
                    }
                }
            }
        });
        return removedDefs;
    }

    private void createBeanDefinitions(Klass klass, List<BeanDefinition> beanDefinitions, BeanDefinitionRegistry registry, IInstanceContext context) {
        BeanDefinition beanDef;
        var beakKind = klass.getAttribute(AttributeNames.BEAN_KIND);
        if (beakKind != null) {
            if (klass.isNew())
                beanDefinitions.add(beanDef = new ComponentBeanDefinition(klass.getAttributeNonNull(AttributeNames.BEAN_NAME), registry, klass));
            else
                beanDef = registry.getBeanDefinitionsByType(klass.getType()).getFirst();
            if (beakKind.equals(BeanKinds.CONFIGURATION)) {
                klass.forEachMethod(method -> {
                    String beanName;
                    if (method.isNew()
                            && (beanName = method.getAttribute(AttributeNames.BEAN_NAME)) != null) {
                        beanDefinitions.add(new FactoryBeanDefinition(beanDef, beanName, registry, method));
                    }
                });
            }
        }
    }

    private void initializeBeanDefinitions(List<BeanDefinition> definitions, BeanDefinitionRegistry registry, IInstanceContext context) {
        var sorted = sortByTopology(definitions, registry);
        for (BeanDefinition beanDef : sorted) {
            beanDef.initialize(registry, context);
        }
    }

    private void destructBean(BeanDefinition beanDefinition, IInstanceContext context) {
        if (beanDefinition.getBean() != null) {
            var bean = beanDefinition.getBean().resolveObject();
            context.remove(bean);
            assert context.internalGet(bean.getId()).isRemoved();
        }
    }

    private List<BeanDefinition> sortByTopology(List<BeanDefinition> definitions, BeanDefinitionRegistry registry) {
        var visiting = new HashSet<BeanDefinition>();
        var visited = new HashSet<BeanDefinition>();
        var sorted = new ArrayList<BeanDefinition>();
        for (var def : definitions) {
            visit(def, visiting, visited, sorted, registry);
        }
        return sorted;
    }

    private void visit(BeanDefinition definition, Set<BeanDefinition> visiting, Set<BeanDefinition> visited, List<BeanDefinition> sorted, BeanDefinitionRegistry registry) {
        if (!visited.add(definition))
            return;
        if (!visiting.add(definition))
            throw new IllegalStateException("Circular dependency detected at " + definition);
        for (BeanDefinition dependency : definition.getDependencies(registry)) {
            if (!dependency.isInitialized())
                visit(dependency, visiting, visited, sorted, registry);
        }
        visiting.remove(definition);
        sorted.add(definition);
    }

}
