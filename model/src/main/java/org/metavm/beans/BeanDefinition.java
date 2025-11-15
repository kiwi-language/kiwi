package org.metavm.beans;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.wire.Parent;
import org.metavm.wire.adapters.ObjectAdapter;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire(adapter = ObjectAdapter.class)
@Entity
public abstract class BeanDefinition extends org.metavm.entity.Entity {

    @Parent
    private final BeanDefinitionRegistry registry;
    @Setter
    @Getter
    private String name;
    private @Nullable Reference bean;

    public BeanDefinition(String name, BeanDefinitionRegistry registry) {
        super(registry.nextChildId());
        this.name = name;
        this.registry = registry;
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

    public void updateBean(BeanDefinitionRegistry registry) {
        var bean = Objects.requireNonNull(getBean(), "Bean not initialized").resolveObject();
        bean.getInstanceType().forEachField(f -> {
            if (f.isPublic() && !f.isStatic()) {
                if (registry.isDependency(f.getPropertyType())) {
                    bean.setField(f.getRawField(), registry.getDependency(f.getPropertyType()));
                }
            }
        });
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
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
