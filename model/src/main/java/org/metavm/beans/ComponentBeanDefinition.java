package org.metavm.beans;

import lombok.extern.slf4j.Slf4j;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@NativeEntity(11)
@Entity
public class ComponentBeanDefinition extends BeanDefinition {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private Reference klassReference;

    public ComponentBeanDefinition(String name, BeanDefinitionRegistry registry, Klass klass) {
        this(name, registry, klass.getReference());
    }

    public ComponentBeanDefinition(String name, BeanDefinitionRegistry registry, Reference klassReference) {
        super(name, registry);
        this.klassReference = klassReference;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        BeanDefinition.visitBody(visitor);
        visitor.visitValue();
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
    public void buildJson(Map<String, Object> map) {
        map.put("beanType", this.getBeanType().toJson());
        map.put("klass", this.getKlass().getStringId());
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
        return EntityRegistry.TAG_ComponentBeanDefinition;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.klassReference = (Reference) input.readValue();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeValue(klassReference);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
