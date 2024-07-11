package org.metavm.beans;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.InstanceReference;
import org.metavm.object.type.ClassType;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@EntityType
public abstract class BeanDefinition extends Entity {

    private String name;
    private @Nullable InstanceReference bean;

    public BeanDefinition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClassInstance getBean() {
        return Objects.requireNonNull(bean).resolveObject();
    }

    public abstract ClassType getBeanType();

    public void initialize(BeanDefinitionRegistry registry, IEntityContext context) {
        if (bean == null)
            bean = createBean(registry, context).getReference();
        else
            throw new IllegalStateException("Bean already created");
    }

    public boolean isInitialized() {
        return bean != null;
    }

    protected abstract ClassInstance createBean(BeanDefinitionRegistry registry, IEntityContext context);

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        return bean != null ? List.of(bean) : List.of();
    }

    public abstract List<BeanDefinition> getDependencies(BeanDefinitionRegistry registry);

}
