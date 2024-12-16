package org.metavm.beans;

import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.KlassType;
import org.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@Entity
public class FactoryBeanDefinition extends BeanDefinition {

    private final BeanDefinition configurationBeanDef;
    private final Method method;

    public FactoryBeanDefinition(BeanDefinition configurationBeanDef, String name, Method method) {
        super(name);
        if(!(method.getReturnType() instanceof KlassType))
            throw new IllegalArgumentException("Factory method " + method.getName() + " does not return a class type");
        this.configurationBeanDef = configurationBeanDef;
        this.method = method;
    }

    @Override
    public ClassType getBeanType() {
        return (ClassType) method.getReturnType();
    }

    @Override
    public ClassInstance createBean(BeanDefinitionRegistry registry, IEntityContext context) {
        return Objects.requireNonNull(Flows.invoke(method.getRef(), configurationBeanDef.getBean(), registry.getFlowArguments(method), context)).resolveObject();
    }

    @Override
    public List<BeanDefinition> getDependencies(BeanDefinitionRegistry registry) {
        return NncUtils.prepend(configurationBeanDef, registry.getFlowDependencies(method));
    }
}
