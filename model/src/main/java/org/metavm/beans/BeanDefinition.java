package org.metavm.beans;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@NativeEntity(62)
@Entity
public abstract class BeanDefinition extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private BeanDefinitionRegistry registry;
    private String name;
    private @Nullable Reference bean;

    public BeanDefinition(String name, BeanDefinitionRegistry registry) {
        this.name = name;
        this.registry = registry;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitNullable(visitor::visitValue);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClassInstance resolveBean() {
        return Objects.requireNonNull(bean, () -> "Bean '" + name + "' is not yet initialized").resolveObject();
    }

    @Nullable
    public Reference getBean() {
        return bean;
    }

    public abstract ClassType getBeanType();

    public void initialize(BeanDefinitionRegistry registry, IInstanceContext context) {
        if (bean == null)
            bean = createBean(registry, context).getReference();
        else
            throw new IllegalStateException("Bean already created");
    }

    public boolean isInitialized() {
        return bean != null;
    }

    protected abstract ClassInstance createBean(BeanDefinitionRegistry registry, IInstanceContext context);

    @Override
    public List<Instance> beforeRemove(IInstanceContext context) {
        return bean != null ? List.of(bean.get()) : List.of();
    }

    public abstract List<BeanDefinition> getDependencies(BeanDefinitionRegistry registry);

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return registry;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (bean != null) action.accept(bean);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("name", this.getName());
        var bean = this.getBean();
        if (bean != null) map.put("bean", bean.toJson());
        map.put("beanType", this.getBeanType().toJson());
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
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_BeanDefinition;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.registry = (BeanDefinitionRegistry) parent;
        this.name = input.readUTF();
        this.bean = input.readNullable(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(name);
        output.writeNullable(bean, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
