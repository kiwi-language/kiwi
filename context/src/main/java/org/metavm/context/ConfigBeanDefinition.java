package org.metavm.context;

import java.util.List;

public abstract class ConfigBeanDefinition<T> extends BeanDefinition<T> {

    public abstract List<BeanDefinition<?>> createInnerDefinitions();

}
