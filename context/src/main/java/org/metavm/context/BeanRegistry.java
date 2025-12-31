package org.metavm.context;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class BeanRegistry {

    public static final BeanRegistry instance = new BeanRegistry();

    private final Map<String, BeanDefinition<?>> beanDefs = new HashMap<>();

    private BeanRegistry() {
    }

    /** @noinspection rawtypes*/
    private List<BeanDefinition> addBeanDefs(Iterable<BeanDefinition> beanDefs) {
        for (BeanDefinition<?> beanDef : beanDefs) {
            if (this.beanDefs.containsKey(beanDef.getName()))
                throw new IllegalStateException("Duplicate bean definition for name: " + beanDef.getName());
            this.beanDefs.put(beanDef.getName(), beanDef);
        }
        for (BeanDefinition<?> beanDef : beanDefs) {
            beanDef.init(this);
        }
        var newDefs = new ArrayList<BeanDefinition>();
        for (BeanDefinition<?> beanDef : beanDefs) {
            if (beanDef instanceof ConfigBeanDefinition<?> configBd) {
                newDefs.addAll(configBd.createInnerDefinitions());
            }
        }
        return newDefs;
    }

    public <T> T getBean(Class<T> cls, String...names) {
        for (String name : names) {
            var bean = findBean(cls, name);
            if (bean != null)
                return bean;
        }
        throw new IllegalStateException("Cannot find bean definition for: " + cls.getName() + " for name(s): " +
                String.join(", ", Arrays.asList(names)));
    }

    private <T> @Nullable T findBean(Class<T> cls, String name) {
        var def = beanDefs.get(name);
        return def != null ? cls.cast(def.getBean()) : null;
    }

    public <T> List<T> getBeans(Class<T> cls, List<String> names) {
        return names.stream().map(name -> findBean(cls, name)).filter(Objects::nonNull).toList();
    }

    public void forEachBean(Consumer<Object> action) {
        for (BeanDefinition<?> bd : beanDefs.values()) {
            action.accept(bd.getBean());
        }
    }

    public void initialize(Set<String> modules) {
        var beanDefs = ServiceLoader.load(BeanDefinition.class, BeanRegistry.class.getClassLoader()).stream()
                .map(ServiceLoader.Provider::get)
                .filter(bd -> bd.getModule() == null || modules.contains(bd.getModule()))
                .toList();
        for(;;) {
            var newDefs = addBeanDefs(beanDefs);
            if (newDefs.isEmpty())
                break;
            beanDefs = newDefs;
        }
        for (BeanDefinition<?> beanDef : this.beanDefs.values()) {
            beanDef.getBean();
        }
    }

}
