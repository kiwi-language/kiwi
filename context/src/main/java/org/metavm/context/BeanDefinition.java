package org.metavm.context;

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

    protected abstract T createBean();

    protected void initBean(T o) {}

    public abstract Class<T> getSupportedType();

    public abstract boolean isPrimary();

}
