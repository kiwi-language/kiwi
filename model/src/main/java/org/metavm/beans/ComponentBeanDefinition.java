package org.metavm.beans;

import org.metavm.api.EntityType;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@EntityType
public class ComponentBeanDefinition extends BeanDefinition {

    private final Klass klass;

    public ComponentBeanDefinition(String name, Klass klass) {
        super(name);
        this.klass = klass;
    }

    @Override
    public ClassType getBeanType() {
        return klass.getType();
    }

    @Override
    protected ClassInstance createBean(BeanDefinitionRegistry registry, IEntityContext context) {
        var c = getConstructor();
        return (ClassInstance) Objects.requireNonNull(Flows.invoke(c, ClassInstance.allocate(klass.getType()), registry.getFlowArguments(c), context));
    }

    @Override
    public List<BeanDefinition> getDependencies(BeanDefinitionRegistry registry) {
        return registry.getFlowDependencies(getConstructor());
    }

    private Method getConstructor() {
        return NncUtils.findRequired(klass.getMethods(), Method::isConstructor);
    }

}
