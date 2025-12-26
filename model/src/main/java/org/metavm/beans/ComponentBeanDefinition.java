package org.metavm.beans;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Utils;

import java.util.List;
import java.util.function.Consumer;

@Wire(11)
@Slf4j
@Entity
public class ComponentBeanDefinition extends BeanDefinition {

    private final Reference klassReference;

    public ComponentBeanDefinition(String name, BeanDefinitionRegistry registry, Klass klass) {
        this(name, registry, klass.getReference());
    }

    public ComponentBeanDefinition(String name, BeanDefinitionRegistry registry, Reference klassReference) {
        super(name, registry);
        this.klassReference = klassReference;
    }

    @Override
    public ClassType getBeanType() {
        return getKlass().getType();
    }

    public Klass getKlass() {
        return (Klass) klassReference.get();
    }

    @Override
    protected ClassInstance createBean(BeanDefinitionRegistry registry, IInstanceContext context) {
        var c = getConstructor();
        var instance = ClassInstance.allocate(context.allocateRootId(), getKlass().getType());
        Flows.invoke(c.getRef(),
                instance,
                registry.getFlowArguments(c),
                context);
        return instance;
    }

    @Override
    public List<BeanDefinition> getDependencies(BeanDefinitionRegistry registry) {
        return registry.getFlowDependencies(getConstructor());
    }

    private Method getConstructor() {
        return Utils.findRequired(getKlass().getMethods(), Method::isConstructor,
                () -> "Cannot find constructor in class " + getKlass().getQualifiedName());
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(klassReference);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
