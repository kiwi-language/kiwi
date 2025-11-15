package org.metavm.beans;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.KlassType;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire(18)
@Entity
public class FactoryBeanDefinition extends BeanDefinition {

    private final Reference configurationBeanDef;
    private final Reference method;

    public FactoryBeanDefinition(BeanDefinition configurationBeanDef, String name, BeanDefinitionRegistry registry, Method method) {
        super(name, registry);
        if(!(method.getReturnType() instanceof KlassType))
            throw new IllegalArgumentException("Factory method " + method.getName() + " does not return a class type");
        this.configurationBeanDef = configurationBeanDef.getReference();
        this.method = method.getReference();
    }

    @Override
    public ClassType getBeanType() {
        return (ClassType) getMethod().getReturnType();
    }

    @Override
    public ClassInstance createBean(BeanDefinitionRegistry registry, IInstanceContext context) {
        return Objects.requireNonNull(Flows.invoke(getMethod().getRef(), getConfigurationBeanDef().resolveBean(), registry.getFlowArguments(getMethod()), context)).resolveObject();
    }

    public BeanDefinition getConfigurationBeanDef() {
        return (BeanDefinition) configurationBeanDef.get();
    }

    public Method getMethod() {
        return (Method) method.get();
    }

    @Override
    public List<BeanDefinition> getDependencies(BeanDefinitionRegistry registry) {
        return Utils.prepend(getConfigurationBeanDef(), registry.getFlowDependencies(getMethod()));
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(configurationBeanDef);
        action.accept(method);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
