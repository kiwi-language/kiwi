package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.beans.BeanDefinition;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.beans.ComponentBeanDefinition;
import org.metavm.beans.FactoryBeanDefinition;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.entity.IEntityContext;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class BeanManager {

    public void createBeans(Collection<Klass> klasses, BeanDefinitionRegistry registry, IEntityContext context) {
        var defs = new ArrayList<BeanDefinition>();
        for (Klass k : klasses)
            createBeanDefinitions(k, defs, registry, context);
        defs.forEach(registry::registerBeanDefinition);
        initializeBeanDefinitions(defs, registry, context);
    }

    private void createBeanDefinitions(Klass klass, List<BeanDefinition> beanDefinitions, BeanDefinitionRegistry registry, IEntityContext context) {
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

    private void initializeBeanDefinitions(List<BeanDefinition> definitions, BeanDefinitionRegistry registry, IEntityContext context) {
        var sorted = sortByTopology(definitions, registry);
        for (BeanDefinition beanDef : sorted) {
            beanDef.initialize(registry, context);
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
