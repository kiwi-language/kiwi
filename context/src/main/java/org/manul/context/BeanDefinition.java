package org.manul.context;

import javax.annotation.Nullable;
import java.util.List;

public abstract class BeanDefinition<T> {

    protected BeanRegistry registry;
    private T bean;
    private boolean creating;

    public void init(BeanRegistry registry) {
        this.registry = registry;
    }

    public abstract String getName();

    public T getBean() {
        if (bean != null)
            return bean;
        if (creating)
            throw new IllegalStateException("Circular dependency detected while creating bean: " + getName());
        creating = true;
        bean = createBean();
        creating = false;
        initBean(bean);
        return bean;
    }

    public List<Initializer> getInitializers() {
        return List.of();
    }

    protected abstract T createBean();

    protected void initBean(T o) {}

    public abstract Class<T> getSupportedType();

    public abstract boolean isPrimary();

    public @Nullable String getModule() {
        return null;
    }

}
