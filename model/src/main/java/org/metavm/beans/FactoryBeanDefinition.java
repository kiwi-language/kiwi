package org.metavm.beans;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassType;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@NativeEntity(18)
@Entity
public class FactoryBeanDefinition extends BeanDefinition {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Reference configurationBeanDef;
    private Reference method;

    public FactoryBeanDefinition(BeanDefinition configurationBeanDef, String name, BeanDefinitionRegistry registry, Method method) {
        super(name, registry);
        if(!(method.getReturnType() instanceof KlassType))
            throw new IllegalArgumentException("Factory method " + method.getName() + " does not return a class type");
        this.configurationBeanDef = configurationBeanDef.getReference();
        this.method = method.getReference();
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        BeanDefinition.visitBody(visitor);
        visitor.visitValue();
        visitor.visitValue();
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
    public void buildJson(Map<String, Object> map) {
        map.put("beanType", this.getBeanType().toJson());
        map.put("configurationBeanDef", this.getConfigurationBeanDef().getStringId());
        map.put("method", this.getMethod().getStringId());
        map.put("name", this.getName());
        var bean = this.getBean();
        if (bean != null) map.put("bean", bean.toJson());
        map.put("initialized", this.isInitialized());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_FactoryBeanDefinition;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.configurationBeanDef = (Reference) input.readValue();
        this.method = (Reference) input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeValue(configurationBeanDef);
        output.writeValue(method);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
